package ca.efriesen.lydia.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.SparseArray;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.Dashboard;
import ca.efriesen.lydia.devices.*;
import java.io.*;
import java.util.Arrays;

/**
 * Created by eric on 2/8/2014.
 */
public class ArduinoService extends Service {

	private static final String TAG = "accessory";

	private Arduino arduino;
	private SparseArray<Device> devices;

	//the first part of this string have to be the package name
	private UsbManager mUsbManager;
	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private ParcelFileDescriptor mFileDescriptor;
	private Thread thread;
	private UsbAccessory mAccessory = null;

	private final IBinder mBinder = new ArduinoBinder();

	// create a new listener to pass to all the sensors.  they use this to send data out via the arduino
	private ArduinoListener listener = new ArduinoListener() {
		@Override
		public void writeData(byte data[], int from) {
			// data has "I have this much data, here it is."  It's missing the "hey you, it's me part"
			// create new array one size bigger
			byte temp[] = new byte[data.length+3];

			// copy the array, and move up one position
			System.arraycopy(data, 0, temp, 2, data.length);
			temp[0] = (byte)from; // hey you (we need the recipient first)
			temp[1] = Device.id; // it's me
			// we need to remove the initial length from the data array, or our checksum will be incorrect
			byte checksum[] = new byte[data.length-1];
			// copy the data into a new array to get the checksum
			System.arraycopy(data, 1, checksum, 0, checksum.length);
			// copy the checksum into our array to be sent over the wire
			temp[temp.length-1] = (byte)getChecksum(getIntArray(checksum));

			// send the data over the wire
			try {
				mOutputStream.write(temp);
			} catch (IOException e) {
				Log.e(TAG, "", e);
			} catch (NullPointerException e) {
				Log.e(TAG, "", e);
			}
		}

		@Override
		public void writeData(int data, int id) {
			try {
				mOutputStream.write(data);
			} catch (IOException e) {
				Log.e(TAG, "", e);
			} catch (NullPointerException e) {
				Log.e(TAG, "", e);
			}
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.d(TAG, "onstartcommand");
		if (intent == null) {
			return START_STICKY;
		}
		if (!intent.hasExtra(UsbManager.EXTRA_ACCESSORY)) {
			Log.d(TAG, "no accessory passed");
			return START_STICKY;
		}

		if (mAccessory == null) {
			Log.d(TAG, "get usbmanager");
			mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
			UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
			Log.d(TAG, "got accessory " + accessory);

			if (accessory != null) {
				// FIXME
				// I can't get the USB_ACCESSORY_DETACHED event to fire.  So this will close the accessory before opening every time
				//closeAccessory();
				openAccessory(accessory);
			}
		}
		return START_STICKY;
	}

	@Override
	public void onCreate() {

		// start it in the foreground so it doesn't get killed
		Notification.Builder builder = new Notification.Builder(this)
				.setSmallIcon(R.drawable.device_access_bluetooth)
				.setContentTitle("Arduino Managerr")
				.setContentText("Arduino Manager");

		// a pending intent for the notification.  this will take us to the dashboard, or main activity
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, Dashboard.class), PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);

		// Add a notification
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(4, builder.build());

		arduino = new Arduino();
		// populate the devices array
		// The device constructor takes a context, the constant that defines the device on the arduino side (just a number) and an intent to fire when data received
		devices = new SparseArray<Device>();
		// populate the devices array with the devices, using their id as the key and the object as the value
		devices.put(Master.id, new Master(this));
		devices.put(IdiotLights.id, new IdiotLights(this));

//		devices.add(new Alarm(this, Constants.ALARM, null));
//		devices.put(Constants.LIGHTSENSOR, new LightSensor(this));
//		devices.add(new MJLJReceiver(this, Constants.MJLJ, null));
//		devices.add(new PressureSensor(this, Constants.FLPRESSURESENSOR, Intents.));
//		devices.put(Constants.INSIDETEMPERATURESENSOR, new TemperatureSensor(this, Intents.INSIDETEMPERATURE));
//		devices.put(Constants.OUTSIDETEMPERATURESENSOR, new TemperatureSensor(this, Intents.OUTSIDETEMPERATURE));

//		devices.put(Constants.REARWINDOWDEFROSTER, new Defroster(this, Intents.DEFROSTER));
//		devices.put(Constants.DRIVERSEAT, new Seats(this, Constants.DRIVERSEAT));
//		devices.put(Constants.PASSENGERSEAT, new Seats(this, Constants.PASSENGERSEAT));
//		devices.add(new Windows(this, Constants.WINDOWS, Intents.WINDOWCONTROL));
//		devices.add(new Wipers(this, Constants.WIPE, Intents.WIPE));

		// pass in the devices to the arduino.
		// set the listener above to all devices
		arduino.setDevices(devices, listener);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// tell each sensor to cleanup
		for (int i=0, size = devices.size(); i<size; i++) {
			devices.valueAt(i).cleanUp();
		}

		closeAccessory();
	}

	public class ArduinoBinder extends Binder {
		public ArduinoService getService() {
			return ArduinoService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private void openAccessory(UsbAccessory accessory) {
		Log.d(TAG, "open accessory");

		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			Log.d(TAG, "accessory opened");
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mInputStream = new FileInputStream(fd);
			mOutputStream = new FileOutputStream(fd);
			mAccessory = accessory;

			thread = new Thread(null, commRunnable, TAG);
			thread.start();
		} else {
			Log.d(TAG, "accessory open failed");
		}
	}

	private void closeAccessory() {
		Log.d(TAG, "close accessory");
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}
		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
			if (mInputStream != null) {
				mInputStream.close();
			}
			if (mOutputStream != null) {
				mOutputStream.close();
			}
		} catch (IOException e) {
		} finally {
			mFileDescriptor = null;
			mInputStream = null;
			mOutputStream = null;
			mAccessory = null;
		}
	}

	private Runnable commRunnable = new Runnable() {
		@Override
		public void run() {
//			Log.d(TAG, "Start new thread for accessory");
			int ret = 0;
			byte[] buffer = new byte[255];

			while (ret >= 0) {
//				Log.d(TAG, "start read");
				try {
					ret = mInputStream.read(buffer);
				} catch (Exception e) {
					Log.d(TAG, "read error");
					Log.d(TAG, e.toString());
					closeAccessory();
					break;
				}

				// bytes are signed.  we need the unsigned version.  Thus the & 0xFF
				// we don't care about who's receiving.  if it's sent to the master, we inspect it
				if (buffer[0] == 0x7e) {
					int recipient = buffer[1] & 0xFF;
					int sender = buffer[2] & 0xFF;
					int length = buffer[3];
//					Log.d(TAG, "recip " + recipient + " sender " + sender + " length " + length);
					// the data starts at position 3, so the end is 3 plus the length.
					byte data[] = Arrays.copyOfRange(buffer, 4, 4 + length);
					// create a new array of ints the same length as the data portion
//					int dataInt[] = new int[data.length];
//					Log.d(TAG, "dataint is " + dataInt.length + " long");
					// loop over everything in the data array
//					for (int i = 0; i < data.length; i++) {
//						// convert the byte into an int, and convert the signed into "unsigned". (java doesn't have unsigned, that's why we need an int for a larger data type)
//						dataInt[i] = data[i] & 0xFF;
////						Log.d(TAG, "data " + i + " is " + data[i]);
//					}
					// get the checksum
					int checksum = buffer[length + 4] & 0xFF;

//					Log.d(TAG, "checksum is " + checksum + " calculated is " + getChecksum(dataInt));
//					Log.d(TAG, "read done");
					int dataInt[] = getIntArray(data);
					if (checksum == getChecksum(dataInt)) {
						arduino.parseData(sender, length, dataInt, checksum);
					}
				}
			}
		}
	};

	public Device getDevice(int id) {
		return devices.get(id);
	}

	private int[] getIntArray(byte data[]) {
		int dataInt[] = new int[data.length];
		for (int i = 0; i < data.length; i++) {
			// convert the byte into an int, and convert the signed into "unsigned". (java doesn't have unsigned, that's why we need an int for a larger data type)
			dataInt[i] = data[i] & 0xFF;
//			Log.d(TAG, "data " + i + " is " + data[i]);
		}
		return dataInt;
	}

	// calculate the checksum of the data we received
	private int getChecksum(int[] data) {
//		Log.d(TAG, "get checksum");
		int XOR = 0;
		for(int d : data) {
//			Log.d(TAG, "data " + d);
			XOR = XOR ^ d;
		}
		return XOR;
	}

	// new listener interface.
	// provide both byte array and int methods.
	public interface ArduinoListener {
		public void writeData(byte data[], int from);
		public void writeData(int data, int from);
	}
}
