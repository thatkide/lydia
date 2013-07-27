package ca.efriesen.lydia_phone.services;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import ca.efriesen.lydia_common.BluetoothService;
import ca.efriesen.lydia_common.includes.Intents;
import ca.efriesen.lydia_common.messages.PhoneCall;
import ca.efriesen.lydia_common.messages.SMS;
import ca.efriesen.lydia_common.media.Song;
import ca.efriesen.lydia_phone.R;
import ca.efriesen.lydia_phone.activities.Lydia;

/**
 * User: eric
 * Date: 2012-10-28
 * Time: 2:13 PM
 */
public class ManagerService extends Service {
	public static final String TAG = "Lydia Phone Manager Service";
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothService mBluetoothService;
	private static final int ONGOING_NOTIFICATION = 1;

	private final IBinder mBinder = new ManagerServiceBinder();

	public class ManagerServiceBinder extends Binder {
		public ManagerService getService() {
			return ManagerService.this;
		}
	}

	public IBinder onBind(Intent intent) {
		return mBinder;
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
				mBluetoothService.startServer();
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

		registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		registerReceiver(bluetoothGetStateReceiver, new IntentFilter(Intents.BLUETOOTHGETSTATE));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			unregisterReceiver(bluetoothStateReceiver);
		} catch (Exception e) {}
	}

	private PhoneStateListener mPhoneListener = new PhoneStateListener() {
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
				case TelephonyManager.CALL_STATE_RINGING: {
					PhoneCall call = new PhoneCall();
					call.setFromNumber(incomingNumber);
					call.setState(TelephonyManager.CALL_STATE_RINGING);
					// send to tablet
					mBluetoothService.write(BluetoothService.objectToByteArray(call));
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

	// get the incoming sms from the other phone
	private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// create a new internal sms object
			SMS sms = new SMS();
			// URI for sms database
			Uri uri = Uri.parse("content://sms/inbox");

			// get the sms from the intent
			Bundle extras = intent.getExtras();

			if (extras != null) {
				// get received SMS array
				Object[] pdus = (Object[]) extras.get("pdus");
				StringBuilder builder = new StringBuilder();

				// loop over the pdus
				for (int i = 0; i < pdus.length; i++) {
					// create a new message from the pdu
					SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
					builder.append(smsMessage.getMessageBody());
					sms.setFromNumber(smsMessage.getOriginatingAddress());
				}

				Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
				cursor.moveToFirst();
				while (cursor.moveToNext()) {
					if ((cursor.getString(cursor.getColumnIndex("address")).equals(sms.getFromNumber())) && (cursor.getInt(cursor.getColumnIndex("read")) == 0)) {
						sms.setId(cursor.getInt(cursor.getColumnIndex("_id")));
						Log.d(TAG, "id is " + sms.getId());
					}
				}

				// add the message
				sms.setMessage(builder.toString());
				mBluetoothService.write(BluetoothService.objectToByteArray(sms));
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
						Intent updateMedia = new Intent(Intents.UPDATEMEDIAINFO);
						updateMedia.putExtra("ca.efriesen.Song", (Song)msg.obj);
						sendBroadcast(updateMedia);
					} else if (msg.obj instanceof SMS) {
						Intent smsReceived = new Intent(Intents.SMSRECEIVED);
						smsReceived.putExtra("ca.efriesen.SMS", (SMS)msg.obj);
						sendBroadcast(smsReceived);
					}
					break;
				}
				case BluetoothService.MESSAGE_CONNECTED: {
					Log.d(TAG, "connected, sending broadcast");
					sendBroadcast(new Intent(Intents.BLUETOOTHMANAGER).putExtra("state", BluetoothService.STATE_CONNECTED));
					break;
				}
				case BluetoothService.MESSAGE_DISCONNECTED: {
					Log.d(TAG, "disconnected, sending broadcast");
					mBluetoothService.stop();
					mBluetoothService.startServer();
					// sends an intent to the ui for display purposes
					sendBroadcast(new Intent(Intents.BLUETOOTHMANAGER).putExtra("state", BluetoothService.STATE_NONE));
					break;
				}
			}
		}
	};

	private BroadcastReceiver bluetoothGetStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			sendBroadcast(new Intent(Intents.BLUETOOTHMANAGER).putExtra("state", mBluetoothService.getState()));
		}
	};

	private BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

			switch (state) {
				case BluetoothAdapter.STATE_OFF: {
					mBluetoothService.stop();
					break;
				}
				case BluetoothAdapter.STATE_ON: {
					mBluetoothService.startServer();
				}
			}
		}
	};
}
