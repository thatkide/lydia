package ca.efriesen.lydia.services;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.Dashboard;
import ca.efriesen.lydia_common.messages.PhoneCall;
import ca.efriesen.lydia_common.messages.SMS;
import ca.efriesen.lydia_common.includes.Constants;
import ca.efriesen.lydia_common.includes.Intents;
import ca.efriesen.lydia_common.media.Song;
import ca.efriesen.lydia.databases.MessagesDataSource;
import ca.efriesen.lydia.devices.*;
import ca.efriesen.lydia.interfaces.SerialIO;
import ca.efriesen.lydia.devices.LightSensor;
import ca.efriesen.lydia.devices.Device;
import ca.efriesen.lydia.devices.TemperatureSensor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * User: eric
 * Date: 2012-10-21
 * Time: 3:02 PM
 */
public class HardwareManagerService extends Service {
	public static final String TAG = "lydia Hardware Manager";
	private final IBinder mBinder = new HardwareManagerBinder();

	// Bluetooth settings
	private BluetoothService bluetoothService;
	private BluetoothAdapter mBluetoothAdapter = null;
	private Thread connectBluetooth;

	// message storage stuff
	MessagesDataSource dataSource;

	// list of devices
	private ArrayList<Device> devices;

	// we bind to this service in the dashboard.java file.
	// the reason i do this is because then we can force an update to be sent over serial to get info we need about the devices and such.
	// this enables us to get the most up to date info as soon as the activity and service are both running
	public class HardwareManagerBinder extends Binder {
		public HardwareManagerService getService() {
			return HardwareManagerService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("useBluetooth", false)) {
			// setup bluetooth stuff
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			bluetoothService = new BluetoothService(mBluetoothHandler);

			// only start the bluetooth stuff if bluetooth is on
			if (mBluetoothAdapter.isEnabled()) {
				// connect bluetooth to the phone
				if (bluetoothService.getState() == BluetoothService.STATE_NONE) {
					try {
						Log.d(TAG, "connecting devices");
						startConnectBluetooth();
					} catch (Exception e) {
						Log.d(TAG, e.toString());
					}
				}
			}
		}

		dataSource = new MessagesDataSource(this);
		dataSource.open();

		// start it in the foreground so it doesn't get killed
		Notification.Builder builder = new Notification.Builder(this)
				.setSmallIcon(R.drawable.home)
				.setContentTitle("Hardware Manager")
				.setContentText("Hardware Manager");

		// a pending intent for the notification.  this will take us to the dashboard, or main activity
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, Dashboard.class), PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);

		// Add a notification
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(1, builder.build());

		// setup the arduino
		Arduino arduino = new Arduino(this);
		arduino.initlize();

		// populate the devices array
		devices = new ArrayList<Device>();
		devices.add(new LightSensor(this, Constants.LIGHTSENSOR, Intents.LIGHTVALUE));
//		devices.add(new PressureSensor(this, Constants.FLPRESSURESENSOR, Intents.));
		devices.add(new TemperatureSensor(this, Constants.INSIDETEMPERATURESENSOR, Intents.INSIDETEMPERATURE));
		devices.add(new TemperatureSensor(this, Constants.OUTSIDETEMPERATURESENSOR, Intents.OUTISETEMPERATURE));

		devices.add(new Defroster(this, Constants.REARWINDOWDEFROSTER, Intents.DEFROSTER));
		devices.add(new Seats(this, Constants.DRIVERSEAT, Intents.SEATHEAT));
		devices.add(new Seats(this, Constants.PASSENGERSEAT, Intents.SEATHEAT));
		devices.add(new Windows(this, Constants.WINDOWS, Intents.WINDOWCONTROL));
		devices.add(new Wipers(this, Constants.WIPE, Intents.WIPE));

		// add the serial io manager to each serial io sensor
		for (Device s : devices) {
			if (s instanceof SerialIO) {
				((SerialIO) s).setIOManager(arduino.getSerialManager());
			}
		}

		// pass in the devices to the arduino
		// in the Arduino class we will filter out so we only have the devices we need
		arduino.setDevices(devices);

		registerReceiver(toggleBluetoothReveiver, new IntentFilter(Intents.BLUETOOTHTOGGLE));
		registerReceiver(smsReplyReceiver, new IntentFilter(Intents.SMSREPLY));
		registerReceiver(mediaInfoReceiver, new IntentFilter(Intents.UPDATEMEDIAINFO));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// tell each sensor to cleanup
		for (Device s : devices) {
			s.cleanUp();
		}
		bluetoothService.stop();
		try {
			unregisterReceiver(mediaInfoReceiver);
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
	}


	/* bluetooth handler */
	// The Handler that gets information back from the BluetoothChatService
	public final Handler mBluetoothHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case BluetoothService.MESSAGE_READ: {
					if (msg.obj instanceof SMS) {
						Intent smsReceived = new Intent(Intents.SMSRECEIVED);
						smsReceived.putExtra("ca.efriesen.SMS", (SMS)msg.obj);
						sendBroadcast(smsReceived);
					} else if(msg.obj instanceof PhoneCall) {
						Intent phoneCall = new Intent(Intents.PHONECALL);
						phoneCall.putExtra(Intents.PHONECALL, (PhoneCall)msg.obj);
						sendBroadcast(phoneCall);
					}
					break;
				}
//					} else if (command[0].equalsIgnoreCase("INCOMINGCALL")) {
//						String incomingNumber = command[1];
//						sendBroadcast(new Intent(Intents.INCOMINGCALL).putExtra("number", incomingNumber));
//					} else if (command[0].equalsIgnoreCase("MEDIA")) {
//						// value will me play, pause, next, etc
//						// The value is set from the globally available intent class.  the same on client and server
//						String value = command[1];
//						sendBroadcast(new Intent(value));
//					} else if(command[0].equalsIgnoreCase("VOLUME")) {
//						AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//						if (command[1].equalsIgnoreCase("DOWN")) {
//							// decrease the volume by 1
//							int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//							mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume - 1, 0);
//						} else {
//							// increase the volume by 1
//							int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//							mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume + 1, 0);
//						}
//					}

//					Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_LONG).show();
				case BluetoothService.MESSAGE_WRITE: {
					break;
				}
				case BluetoothService.MESSAGE_CONNECTED: {
					Log.d(TAG, "connected, sending broadcast");
					sendBroadcast(new Intent(Intents.BLUETOOTHMANAGER).putExtra("state", Intents.BLUEOOTHCONNECTED));
					killConnectBluetooth();
					break;
				}
				case BluetoothService.MESSAGE_DISCONNECTED: {
					Log.d(TAG, "disconnected, sending broadcast");
					sendBroadcast(new Intent(Intents.BLUETOOTHMANAGER).putExtra("state", Intents.BLUEOOTHDISCONNECTED));
					Log.d(TAG, "starting connect thread");
					startConnectBluetooth();
					break;
				}
				case BluetoothService.MESSAGE_FAILED: {
					bluetoothService.stop();
					break;
				}
			}
		}
	};


	private void killConnectBluetooth() {
		if (connectBluetooth != null) {
			Log.d(TAG, "killing connect thread");
			connectBluetooth.interrupt();
			connectBluetooth = null;
		}
	}

	private void startConnectBluetooth() {
		Log.d(TAG, "starting bluetooth connect");
		connectBluetooth = new Thread(new Runnable() {
			@Override
			public void run() {
			while (true) {
				Log.d(TAG, "connect thread");
				int state = bluetoothService.getState();
				Log.d(TAG, "state " + state);
				if (state != BluetoothService.STATE_CONNECTED && state != BluetoothService.STATE_CONNECTING) {
					// Get the BluetoothDevice object
					String address = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.PhoneAddress, null);
					Log.d(TAG, "saved address" + address);
					if (!BluetoothAdapter.checkBluetoothAddress(address)) {
						Log.d(TAG, "invalid bluetooth address, finishing");
						return;
					}
					BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

					if (device != null) {
						Log.d(TAG, "connecting");
						bluetoothService.connect(device);
					}
				}

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// I said DIE
					Thread.currentThread().interrupt();
					// return, this ensures we quit
					return;
				}
			}
			}
		});
		connectBluetooth.start();
	}

	private BroadcastReceiver toggleBluetoothReveiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// connect bluetooth to the phone
			if (bluetoothService.getState() != BluetoothService.STATE_NONE) {
				bluetoothService.stop();
//				connectBluetooth.interrupt();
			} else {
//				connectBluetooth.start();
			}
		}
	};

	private BroadcastReceiver smsReplyReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			SMS sms = (SMS) intent.getSerializableExtra("ca.efriesen.SMS");

			// store message in db
			dataSource.createMessage(sms.getMessage(), sms.getToNumber(), ca.efriesen.lydia.databases.Message.TYPE_SMS, true);

			bluetoothService.write(BluetoothService.objectToByteArray(sms));
		}
	};

	private BroadcastReceiver mediaInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Song song = (Song) intent.getSerializableExtra("ca.efriesen.Song");

			bluetoothService.write(BluetoothService.objectToByteArray(song));
		}
	};

//	private BroadcastReceiver updateBrightnessReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (ioioBound) {
//				OutputStream outputStream = ioioService.getOutputStream();
//				try {
//					outputStream.write(myIOIOService.UPDATEBRIGHTNESS);
//					outputStream.write(intent.getIntExtra("light", 0));
//					outputStream.write(intent.getIntExtra("brightness", 25));
//				} catch (IOException e) {
//				} catch (NullPointerException e) {
//				}
//			}
//		}
//	};
//
//
//
}
