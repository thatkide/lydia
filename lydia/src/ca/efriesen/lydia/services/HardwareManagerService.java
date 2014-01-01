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
import ca.efriesen.lydia.databases.BluetoothDeviceDataSource;
import ca.efriesen.lydia_common.BluetoothService;
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
	private BluetoothService bluetoothService = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	private Thread connectBluetooth = null;

	// message storage stuff
	MessagesDataSource messagesDataSource;

	// list of devices
	private ArrayList<Device> devices;
	private Arduino arduino;

	// Thread var
	private volatile boolean connectThreadAlive = true;

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

		// if we have said to use bluetooth
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("systemBluetooth", false)) {
			Log.d(TAG, "yepp, bluetooth is on");
			// get the adapter if we havne't already
			if (mBluetoothAdapter == null) {
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			}
			if (!mBluetoothAdapter.isEnabled()) {
				// turn on bluetooth
				mBluetoothAdapter.enable();
			}
		}

		// setup bluetooth stuff
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("useBluetooth", false)) {
			setupBluetooth();
		}
		registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

		messagesDataSource = new MessagesDataSource(this);
		messagesDataSource.open();

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
		arduino = new Arduino(this, PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("autoUpgradeFirmware", true));
		arduino.initialize();

		registerReceiver(upgradeFirmwareReceiver, new IntentFilter("upgradeFirmware"));

		// populate the devices array
		// The device constructor takes a context, the constant that defines the device on the arduino side (just a number) and an intent to fire when data received
		devices = new ArrayList<Device>();
		devices.add(new Alarm(this, Constants.ALARM, null));
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
		for (Device device : devices) {
			if (device instanceof SerialIO) {
				((SerialIO) device).setIOManager(arduino.getSerialManager());
			}
		}

		// pass in the devices to the arduino
		// in the Arduino class we will filter out so we only have the devices we need
		arduino.setDevices(devices);

		registerReceiver(smsReplyReceiver, new IntentFilter(Intents.SMSREPLY));
		registerReceiver(mediaInfoReceiver, new IntentFilter(Intents.UPDATEMEDIAINFO));
		registerReceiver(bluetoothManagerReceiver, new IntentFilter(Intents.BLUETOOTHMANAGER));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopConnectThread();
		bluetoothService.stop();
		try {
			unregisterReceiver(bluetoothStateReceiver);
		} catch (Exception e) {}
		// tell each sensor to cleanup
		for (Device s : devices) {
			s.cleanUp();
		}
		try {
			unregisterReceiver(mediaInfoReceiver);
		} catch (Exception e) {}
		try {
			unregisterReceiver(bluetoothManagerReceiver);
		} catch (Exception e) {}
		try {
			unregisterReceiver(smsReplyReceiver);
		} catch (Exception e) {	}
		try {
			unregisterReceiver(upgradeFirmwareReceiver);
		} catch (Exception e) {}
		arduino.cleanUp();
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
						smsReceived.putExtra(Intents.SMSRECEIVED, (SMS)msg.obj);
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
					sendBroadcast(new Intent(Intents.BLUETOOTHMANAGER).putExtra("state", BluetoothService.STATE_CONNECTED).putExtra("device", (BluetoothDevice) msg.obj));
					// interrupt the sleep, notifiy we are connected
					connectBluetooth.interrupt();
					break;
				}
				case BluetoothService.MESSAGE_DISCONNECTED: {
					Log.d(TAG, "disconnected, sending broadcast");
					sendBroadcast(new Intent(Intents.BLUETOOTHMANAGER).putExtra("state", BluetoothService.STATE_NONE));
					break;
				}
			}
		}
	};

	private void setupBluetooth() {
		if (bluetoothService == null)
			Log.d(TAG, "mmm, new device");
			bluetoothService = new BluetoothService(mBluetoothHandler);

		try {
			Log.d(TAG, "connecting devices");
			startConnectBluetooth();
		} catch (Exception e) {
		//	resetBluetoothService();
			Log.d(TAG, e.toString());
		}
	}

	private void stopConnectThread() {
		connectThreadAlive = false;
		if (connectBluetooth != null) {
			connectBluetooth = null;
		}
	}

	private void startConnectBluetooth() {
		connectThreadAlive = true;
		Log.d(TAG, "starting bluetooth connect");
		connectBluetooth = new Thread(new Runnable() {
			@Override
			public void run() {
				// query the db for all of our devices
				BluetoothDeviceDataSource bluetoothDeviceDataSource;
				bluetoothDeviceDataSource = new BluetoothDeviceDataSource(getApplicationContext());
				bluetoothDeviceDataSource.open();
				ArrayList<BluetoothDevice> devices = bluetoothDeviceDataSource.getAllDevices();
				bluetoothDeviceDataSource.close();
				// while the outer var is true, keep looping forever
				while (connectThreadAlive) {
					// get the connection state
					int state = bluetoothService.getState();
					// if we aren't connected or connecting
					if (state != BluetoothService.STATE_CONNECTED && state != BluetoothService.STATE_CONNECTING) {
						// loop over each device and try to connect
						for (BluetoothDevice device : devices) {
							// Get the BluetoothDevice object
							Log.d(TAG, "trying address " + device.getAddress());

							// connect
							bluetoothService.connect(device);

							// sleep for 5 seconds
							// when we wake up, we'll try the next device
							try {
								Log.d(TAG, "thread sleeping");
								Thread.sleep(5000);
							// when we successfully connect, the thread will be interrupted and we'll exit the for loop
							} catch (InterruptedException e) { }

							// if we connected, break the for loop
							// else try the next device in the db
							if (bluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
								Log.d(TAG, "breaking out of the loop");
								break;
							}
						}
					}
					try {
						// just sleep for a second so we aren't running full go all the time
						Thread.sleep(1000);
					} catch (InterruptedException e) { }

				}
			}
		});

		connectBluetooth.start();
	}

	private BroadcastReceiver upgradeFirmwareReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "stk - upgrading firmware");
			arduino.upgradeFirmware();
		}
	};

	private BroadcastReceiver smsReplyReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			SMS sms = (SMS) intent.getSerializableExtra(Intents.SMSRECEIVED);

			// store message in db
			messagesDataSource.createMessage(sms.getMessage(), sms.getToNumber(), ca.efriesen.lydia.databases.Message.TYPE_SMS, true);

			bluetoothService.write(BluetoothService.objectToByteArray(sms));
		}
	};

	private BroadcastReceiver mediaInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Song song = (Song) intent.getSerializableExtra("ca.efriesen.Song");

			try {
				bluetoothService.write(BluetoothService.objectToByteArray(song));
			} catch (Exception e) {}
		}
	};


	// system bluetooth setting
	private BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			Log.d(TAG, "broadcast received");
			switch (state) {
				case BluetoothAdapter.STATE_OFF: {
					Log.d(TAG, "bluetooth off");
					stopConnectThread();
					break;
				}
				case BluetoothAdapter.STATE_ON: {
					Log.d(TAG, "bluetooth on");
					// in app bluetooth setting
					if (getSharedPreferences(HardwareManagerService.this.getPackageName() + "_preferences", MODE_MULTI_PROCESS).getBoolean("useBluetooth", false)) {
						Log.d(TAG, "ok, set it up then");
						setupBluetooth();
					}
				}
			}
		}
	};

	private BroadcastReceiver bluetoothManagerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra("useBluetooth")) {
				if (intent.getBooleanExtra("useBluetooth", false)) {
					// ensure bluetooth is enabled
					Log.d(TAG, "in app bt on");
					setupBluetooth();
				} else {
					Log.d(TAG, "in app bt off");
					stopConnectThread();
					try {
						bluetoothService.stop();
					} catch (Exception e) {}
				}
			}
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
