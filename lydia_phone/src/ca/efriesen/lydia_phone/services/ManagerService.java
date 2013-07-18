package ca.efriesen.lydia_phone.services;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.os.*;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import ca.efriesen.lydia_common.media.Song;
import ca.efriesen.lydia_phone.R;
import ca.efriesen.lydia_phone.activities.Lydia;
import includes.Intents;

/**
 * User: eric
 * Date: 2012-10-28
 * Time: 2:13 PM
 */
public class ManagerService extends Service {
	public static final String TAG = "Lydia Phone Manager Service";
	public final String SMS_EXTRA_NAME = "pdus";
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothService mBluetoothService;
	private static final int ONGOING_NOTIFICATION = 1;
	private KeyguardManager.KeyguardLock lock;

	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "starting manager");

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			// no bluetooth. no point in continuing
			this.stopSelf();
			return;
		}

		// only start the bluetooth stuff if bluetooth is on
		if (mBluetoothAdapter.isEnabled()) {
			if (mBluetoothService == null) {
				mBluetoothService = new BluetoothService(mHandler);
				// start the server portion
				mBluetoothService.start();
			}
		}

		// listen for sms messages
		registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));

		// listen for phone call state changes
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

		// start it in the foreground so it doesn't get killed
		Notification.Builder builder = new Notification.Builder(this)
				                               .setSmallIcon(R.drawable.ic_launcher)
				                               .setContentTitle("Lydia")
				                               .setContentText("Lydia");

		// a pending intent for the notification.  this will take us to the dashboard, or main activity
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, Lydia.class), PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);

		// Add a notification
		Notification notification = builder.build();
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(1, notification);

		startForeground(ONGOING_NOTIFICATION, notification);

		registerReceiver(sendMessageReceiver, new IntentFilter(Intents.SENDMESSAGE));
	}

	private PhoneStateListener mPhoneListener = new PhoneStateListener() {
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
				case TelephonyManager.CALL_STATE_RINGING: {
					String message = "INCOMINGCALL" + BluetoothService.MESSAGE_DELIMETER + incomingNumber;
					// send to tablet
					mBluetoothService.write(message.getBytes());
					break;
				}
				case TelephonyManager.CALL_STATE_OFFHOOK: {
					// do something
					break;
				}
				case TelephonyManager.CALL_STATE_IDLE: {
					// do something
					break;
				}
				default: {
					Log.d(TAG, "Unknown call state");
				}
			}
		}
	};

	private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Got text");
			// get the sms from the intent
			Bundle extras = intent.getExtras();

			if (extras != null) {
				// get received SMS array
				Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);

				for (int i = 0; i < smsExtra.length; i++) {
					SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
					String message = "SMS" + BluetoothService.MESSAGE_DELIMETER + sms.getOriginatingAddress() + BluetoothService.MESSAGE_DELIMETER + sms.getMessageBody();
					Log.d(TAG, message);
					mBluetoothService.write(message.getBytes());
				}
			}
		}
	};

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case BluetoothService.MESSAGE_READ: {
					Log.d(TAG, "handler");
					if (msg.obj instanceof Song) {
						Song song = (Song) msg.obj;

						Intent updateMedia = new Intent(Intents.UPDATEMEDIAINFO);
						updateMedia.putExtra("ca.efriesen.Song", song);
						sendBroadcast(updateMedia);
					}

//					byte[] readBuf = (byte[]) msg.obj;
//					// construct a string from the valid bytes in the buffer
//					String readMessage = new String(readBuf, 0, msg.arg1);
//					Log.d(TAG, readMessage);
//					String command[] = readMessage.split(BluetoothService.MESSAGE_DELIMETER);

//					if (msg.obj
//					if ("SMS".equalsIgnoreCase(command[0])) {
//						String phoneNumber = command[1];
//						String message = command[2];
//						Log.d(TAG, phoneNumber);
//						Log.d(TAG, message);
//						SmsManager sms = SmsManager.getDefault();
//						try {
//							ArrayList<String> msgStringArray = sms.divideMessage(message);
//							// send the text via cell radio
//							sms.sendMultipartTextMessage(phoneNumber, null, msgStringArray, null, null);
//
//							ContentValues values = new ContentValues();
//							values.put("address", phoneNumber);
//							values.put("body", message);
//							// insert into the sms conversation send list
//							getContentResolver().insert(Uri.parse("content://sms/sent"), values);
//						} catch (IllegalArgumentException e) {
//							Log.d(TAG, "Text failed " + e);
//						}
//					} else if ("PHONE".equalsIgnoreCase(command[0])) {
//						String phoneNumber = command[1];
//						Log.d(TAG, phoneNumber);
//
//						Intent call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
//						call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//						startActivity(call);
//					} else if("MEDIAINFO".equalsIgnoreCase(command[0])) {
//						try {
//							Log.d(TAG, command[1]);
//
//							byte[] buff = Arrays.copyOfRange(readBuf, command[0].length(), readBuf.length);
//							Log.d(TAG, "read buff " + readBuf);
//							Log.d(TAG, "buff " + buff);
//							ByteArrayInputStream bis = new ByteArrayInputStream(buff);
//							ObjectInputStream in = new ObjectInputStream(bis);
//							Song song = (Song) in.readObject();
//							in.close();
//
//							Log.d(TAG, "song name" + song.getName());
////							String uri = command[1];
////							Intent mediaInfo = Intent.parseUri(uri, 0);
////							sendBroadcast(mediaInfo);
////						} catch (URISyntaxException e) {
////							Log.d(TAG, "Invalid URI Intent");
////							Log.d(TAG, e.toString());
//						} catch (NullPointerException e) {
//							Log.e(TAG, e.toString());
//						} catch (StreamCorruptedException e) {
//							Log.e(TAG, e.toString());
//							e.printStackTrace();
//						} catch (IOException e) {
//							Log.e(TAG, e.toString());
//						} catch (ClassNotFoundException e) {
//							Log.e(TAG, e.toString());
//						}
//					}
//					break;
				}
				case BluetoothService.MESSAGE_CONNECTED: {
					Log.d(TAG, "connected, sending broadcast");
					sendBroadcast(new Intent(Intents.BLUETOOTHMANAGER).putExtra("state", Intents.BLUEOOTHCONNECTED));
					break;
				}
				case BluetoothService.MESSAGE_DISCONNECTED: {
					Log.d(TAG, "disconnected, sending broadcast");
					mBluetoothService.stop();
					mBluetoothService.start();
					sendBroadcast(new Intent(Intents.BLUETOOTHMANAGER).putExtra("state", Intents.BLUEOOTHDISCONNECTED));
					break;
				}
				case BluetoothService.MESSAGE_FAILED: {
					mBluetoothService.stop();
					mBluetoothService.start();
					break;
				}
			}
		}
	};

	// used for simple key->value pairs... like "media-~-stop" or "media-~-play"
	private BroadcastReceiver sendMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// get the key and value from the intent
			String key = intent.getStringExtra("key");
			String value = intent.getStringExtra("value");
			// combine them, and delineate
			String message = key + BluetoothService.MESSAGE_DELIMETER + value;
			// send via bluetooth
			mBluetoothService.write(message.getBytes());
		}
	};
}
