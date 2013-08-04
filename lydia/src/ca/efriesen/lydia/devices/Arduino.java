package ca.efriesen.lydia.devices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.util.Log;
import ca.efriesen.lydia.interfaces.SerialIO;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by eric on 2013-05-28.
 */
public class Arduino {
	// debug tag
	private static final String TAG = "lydia Arduino";

	// application context
	private Context context;
	// array list of devices passed in
	private ArrayList<Device> devices = new ArrayList<Device>();

	// usb stuff for the arduino
	private UsbSerialDriver mSerialDevice;
	private UsbManager mUsbManager;

	// this is the buffer for the data received from the arduino
	private StringBuffer serialData = new StringBuffer(64);

	private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
	private SerialInputOutputManager mSerialIoManger;

	// listener for new data
	private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() {
		@Override
		public void onNewData(final byte[] data) {
			// pass new data to our parse method
			parseSerialData(data);
		}

		@Override
		public void onRunError(Exception e) {
			// error, restart the io
			restartIoManager();
		}
	};

	// constructor.
	// pass in the application context so we do things like register broadcast receivers
	public Arduino(Context context) {
		this.context = context;
	}

	// initialize the Arduino
	public void initlize() {
		mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

		// register a receiver, so if the service is already running, and the arduino is attached/detached, we can (re)initialize it and get the io working
		context.registerReceiver(usbAttachReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
		context.registerReceiver(usbDetachReceiver, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));

		// get the arduino working
		initSerialDevice();
	}

	public void cleanUp() {
		// stop io manager
		stopIoManager();
		// close off the device
		if (mSerialDevice != null) {
			try {
				mSerialDevice.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mSerialDevice = null;
		}
		try {
			context.unregisterReceiver(usbAttachReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			context.unregisterReceiver(usbDetachReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SerialInputOutputManager getSerialManager() {
		return mSerialIoManger;
	}

	// take the list of devices from the hardware manager, filter them so we only have serial io devices, then add them to our own list
	public void setDevices(ArrayList<Device> devices) {
		for (Device device : devices) {
			if (device instanceof SerialIO) {
				// only add serial IO devices.  the rest will just wast time and space
				this.devices.add(device);
			}
		}
	}

	// get the device ready to rock and roll
	private void initSerialDevice() {
		try {
			mSerialDevice = UsbSerialProber.acquire(mUsbManager);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "resumed , mSerialDevice=" + mSerialDevice);
		if (mSerialDevice != null) {
			try {
				mSerialDevice.open();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				mSerialDevice.setParameters(115200, 8, 1, 0);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			Log.d(TAG, "no serial devices");
		}
		// (re)start the manager
		restartIoManager();
	}

	// stop the manager
	private void stopIoManager() {
		if (mSerialIoManger != null) {
			Log.d(TAG, "stopping io manager");
			mSerialIoManger.stop();
			mSerialIoManger = null;
		}
	}

	// start the manager
	private void startIoManager() {
		if (mSerialDevice != null) {
			Log.d(TAG, "starting io manager");
			mSerialIoManger = new SerialInputOutputManager(mSerialDevice, mListener);
			mExecutor.submit(mSerialIoManger);
		}
	}

	private void restartIoManager() {
		stopIoManager();
		startIoManager();
	}

	// Takes the byte array received from the arduino, adds it to a buffer, checks for valid control chars, parses the valid strings into an array list, then executes the commands received
	synchronized private void parseSerialData(byte[] data) {
		// remove white space from the byte array, and append it to the buffer
		serialData.append(new String(data).trim());

//		Log.d(TAG, " -------------------");
//		Log.d(TAG, "data: " + serialData.toString());
//		Log.d(TAG, "added: " + new String(data).trim());

		// initialize some variables
		ArrayList<String> words = new ArrayList<String>();
		// look for the first carrot and the first pipe
		int firstCarrot = serialData.indexOf("^");
		int firstPipe = serialData.indexOf("|");

		// if we have no valid control chars "^" or "|", delete everything
		if (firstCarrot == -1 && firstPipe == -1) {
//			Log.d(TAG, "emptying buffer, it's garbage");
			serialData.delete(0, serialData.length());
		}

		// if the first carrot isn't the first character (after the append has happened), then delete everything before.  it's initial garbage
		// -1 happens if there isn't a carrot char "^"
		if (firstCarrot > 0) {
//			Log.d(TAG, "we have a carrot (id " + firstCarrot + "), but garbage before it: " + serialData.toString());
			serialData.delete(0, firstCarrot);
		}

		// we have either a carrot or a pipe, not both.. just return and wait, hopefully the rest is coming
		if (firstCarrot == -1 || firstPipe == -1) {
//			Log.d(TAG, "we're missing a control char... return and wait: " + serialData.toString());
			return;
		}

		// while we have valid data
		// above we check for the presence of a carrot and a pipe.  so we can (somewhat) safely assume we have at least some valid data
		while (serialData.indexOf("|") != -1 && serialData.indexOf("^") != -1) {
			// ensure these are updated
			firstCarrot = serialData.indexOf("^");
			firstPipe = serialData.indexOf("|");
			if (firstCarrot > firstPipe) {
//				Log.d(TAG, "invalid data, delete it " + serialData.toString());
				// we have some invalid data... delete it
				serialData.delete(0, firstCarrot);
				// refresh these
				firstCarrot = serialData.indexOf("^");
				firstPipe = serialData.indexOf("|");
			}

//			Log.d(TAG, "valid command: " + serialData.toString());
//			Log.d(TAG, "first carrot: " + firstCarrot + " first pipe: " + firstPipe);
			// add the current command to the array list
			words.add(serialData.toString().substring(firstCarrot, firstPipe + 1));
			// then remove it from the buffer
			serialData.delete(firstCarrot, firstPipe+1);
		}

		// loop over all the lines we got
		while (!words.isEmpty()) {
			try {
				// split each line based on comma delineated string
				ArrayList<String> commands = new ArrayList<String>(Arrays.asList(words.get(0).split(",")));

				// get teh command and value form the array list
				int command = Integer.valueOf(commands.get(1));
				String value = commands.get(2);

				// loop over each sensor, and check if it matches what's been passed (based on the id associated when it was created)
				for (Device device : devices) {
					if (device.getId() == command) {
						// we found it, so set the value
						device.setValue(value);
						// and break
						break;
					}
				}
				// we were successful, so remove this command
				words.remove(0);
			} catch (IndexOutOfBoundsException e ){
				words.remove(0);
				Log.d(TAG, "we have some invalid info... skip it");
			}
		}
	}

	private BroadcastReceiver usbAttachReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "restarting io");
			// init the deivce
			initSerialDevice();
		}
	};

	private BroadcastReceiver usbDetachReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "stopping io");
			// kill it
			stopIoManager();
		}
	};
}
