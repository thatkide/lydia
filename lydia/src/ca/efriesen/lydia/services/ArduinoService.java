package ca.efriesen.lydia.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import ca.efriesen.lydia.devices.*;
import ca.efriesen.lydia_common.includes.Constants;
import ca.efriesen.lydia_common.includes.Intents;
import java.io.*;
import java.util.Arrays;

/**
 * Created by eric on 2/8/2014.
 */
public class ArduinoService extends Service{

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

	// create a new listener to pass to all the sensors.  they use this to send data out via the arduino
	private ArduinoListener listener = new ArduinoListener() {
		@Override
		public void writeData(byte data[]) {
			try {
				mOutputStream.write(data);
			} catch (IOException e) {
				Log.e(TAG, "", e);
			}
		}

		@Override
		public void writeData(int data) {
			try {
				mOutputStream.write(data);
			} catch (IOException e) {
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
		// setup the arduino
		arduino = new Arduino(this, PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("autoUpgradeFirmware", true));
		arduino.initialize();

		registerReceiver(upgradeFirmwareReceiver, new IntentFilter("upgradeFirmware"));

		// populate the devices array
		// The device constructor takes a context, the constant that defines the device on the arduino side (just a number) and an intent to fire when data received
		devices = new SparseArray<Device>();
//		devices.add(new Alarm(this, Constants.ALARM, null));
		devices.put(Constants.LIGHTSENSOR, new LightSensor(this));
//		devices.add(new MJLJReceiver(this, Constants.MJLJ, null));
//		devices.add(new PressureSensor(this, Constants.FLPRESSURESENSOR, Intents.));
		devices.put(Constants.INSIDETEMPERATURESENSOR, new TemperatureSensor(this, Intents.INSIDETEMPERATURE));
		devices.put(Constants.OUTSIDETEMPERATURESENSOR, new TemperatureSensor(this, Intents.OUTSIDETEMPERATURE));

		devices.put(Constants.REARWINDOWDEFROSTER, new Defroster(this, Intents.DEFROSTER));
		devices.put(Constants.DRIVERSEAT, new Seats(this, Constants.DRIVERSEAT));
		devices.put(Constants.PASSENGERSEAT, new Seats(this, Constants.PASSENGERSEAT));
//		devices.add(new Windows(this, Constants.WINDOWS, Intents.WINDOWCONTROL));
//		devices.add(new Wipers(this, Constants.WIPE, Intents.WIPE));

		// pass in the devices to the arduino
		arduino.setDevices(devices, listener);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// tell each sensor to cleanup
		for (int i=0, size = devices.size(); i<size; i++) {
			devices.valueAt(i).cleanUp();
		}

		try {
			unregisterReceiver(upgradeFirmwareReceiver);
		} catch (Exception e) {}
		closeAccessory();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	private BroadcastReceiver upgradeFirmwareReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "stk - upgrading firmware");
			arduino.upgradeFirmware();
		}
	};

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
			int ret = 0;
			byte[] buffer = new byte[255];

			while (ret >= 0) {
				try {
					ret = mInputStream.read(buffer);
				} catch (Exception e) {
					Log.d(TAG, e.toString());
					closeAccessory();
					break;
				}

				// bytes are signed.  we need the unsigned version
				int target = buffer[0] & 0xFF;
				int length = buffer[1];
				// the data starts at position 2, so the end is 2 plus the length
				byte data[] = Arrays.copyOfRange(buffer, 2, 2+length);

				arduino.parseSerialData(target, length, data);
			}
		}
	};


	// new listener interface.
	// provide both byte array and int methods.
	public interface ArduinoListener {
		public void writeData(byte data[]);
		public void writeData(int data);
	}
}
