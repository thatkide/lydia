package ca.efriesen.lydia.devices;

import android.content.*;
import android.hardware.usb.UsbManager;
import android.preference.PreferenceManager;
import android.util.Log;
import ca.efriesen.lydia.includes.ConstantsStk500v1;
import ca.efriesen.lydia.includes.Hex;
import ca.efriesen.lydia.interfaces.SerialIO;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

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
	private volatile UsbSerialDriver mSerialDevice;
	private UsbManager mUsbManager;

	// this is the buffer for the data received from the arduino
	private StringBuffer serialData = new StringBuffer(64);

	private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
	private volatile SerialInputOutputManager mSerialIoManger;

	// upgrading firmware stuff
	private AtomicBoolean upgradingFirmware = new AtomicBoolean();
	private Thread upgradeThread;
	private volatile byte[] upgradingInput;
	private AtomicBoolean waitingForSerialData = new AtomicBoolean();
	private Boolean checkFirmware = false;

	// listener for new data
	private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() {
		@Override
		public void onNewData(final byte[] data) {
			// do this part if we're not upgrading the firmware (look for out serial data format)
			if (!upgradingFirmware.get()) {
				// pass new data to our parse method
				parseSerialData(data);
			} else {
//				Log.d(TAG, "got new data. length " + data.length);
				upgradingInput = new byte[data.length];
				upgradingInput = data;
				waitingForSerialData.getAndSet(false);
			}
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

	public Arduino(Context context, boolean checkFirmware) {
		this.context = context;
		this.checkFirmware = checkFirmware;
	}

	// initialize the Arduino
	public void initlize() {
		mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

		// register a receiver, so if the service is already running, and the arduino is attached/detached, we can (re)initialize it and get the io working
		context.registerReceiver(usbAttachReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
		context.registerReceiver(usbDetachReceiver, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));

		// get the arduino working
		initSerialDevice();

		if (checkFirmware) {
			checkFirmware();
		}
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
				device.initialize();
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
//		Log.d(TAG, "resumed , mSerialDevice=" + mSerialDevice);
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
//			Log.d(TAG, "stopping io manager");
			mSerialIoManger.stop();
			mSerialIoManger = null;
		}
	}

	// start the manager
	private void startIoManager() {
		if (mSerialDevice != null) {
//			Log.d(TAG, "starting io manager");
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

				// remove the carrot from the beginning
				commands.remove(0);
				// remove the pipe from the end
				commands.remove(commands.size()-1);

				// loop over each sensor, and check if it matches what's been passed (based on the id associated when it was created)
				for (Device device : devices) {
					if (device.getId() == Integer.parseInt(commands.get(0))) {
						// we found it, so set the value
						device.setValue(commands);
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

	public void checkFirmware() {
		try {
			// start a new md5 digest
			MessageDigest digester = MessageDigest.getInstance("MD5");
			// open the hex file
			InputStream inputStream = context.getAssets().open("master.hex");
			// create a new byte array the proper length
			byte[] bytes = new byte[inputStream.available()];
			int byteCount;
			// read the stream and update the digester
			while ((byteCount = inputStream.read()) > 0 ) {
				digester.update(bytes, 0, byteCount);
			}
			// get the digest of all of it
			byte [] digest = digester.digest();
			// create a new buffer so we can get the actual hash
			StringBuffer hexString = new StringBuffer();
			// add the bytes to the string
			for (int i=0; i<digest.length; i++) {
				hexString.append(Integer.toHexString(0xFF & digest[i]));
			}
			// store the hash in shared pref
			PreferenceManager.getDefaultSharedPreferences(context).edit().putString("currentFirmwareDigest", hexString.toString()).apply();
			// get the previous hash
			String previousFirmware = PreferenceManager.getDefaultSharedPreferences(context).getString("previousFirmwareDigest", "");
			// compare them, if they don't equal, update the firmware
			if (!previousFirmware.equalsIgnoreCase(hexString.toString())) {
				PreferenceManager.getDefaultSharedPreferences(context).edit().putString("previousFirmwareDigest", hexString.toString()).apply();
				upgradeFirmware();
			}


		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "no such digest");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void upgradeFirmware() {
		upgradingFirmware.getAndSet(true);
		upgradeThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "staring upload");

				// pass the context and file name to the hex parser
				Hex hex = new Hex(context, "master.hex");
				byte[] hexData = hex.getHexLine(0, hex.getDataSize());

				// reset and then try to sync
				reset();

				// wait for data to return
				waitingForSerialData.set(true);
				getSynchronization();
				// do nothing while waiting.  the async nature makes things difficult
				while (waitingForSerialData.get()) {
					Thread.yield();
				}
				// we're upgrading, so check the input from the get sync command
				// if it's valid, continue
				// check if we got valid input
				if (!checkInput(upgradingInput)) {
					Log.d(TAG, "input is invalid after get sync");
					return;
				}
				// enter programming mode
				enterProgrammingMode();
				// program the flash in the arduino, getting the hex data from the parser
				programFlash(hexData);
				// exit proramming mode, we're done
				exitProgrammingMode();
				// we're done, reset
				reset();
				upgradingFirmware.set(false);
			}
		});
		upgradeThread.start();
	}

	private boolean checkInput(byte[] data) {
		Log.d(TAG, "received data " + data);
		for (int i=0; i<data.length; i++) {
			Log.d(TAG, "index " + i + " is " + Integer.toHexString(data[i]));
		}
		if (data.length == 2 && data[0] == ConstantsStk500v1.STK_INSYNC && data[1] == ConstantsStk500v1.STK_OK) {
			Log.d(TAG, "IN SYNC");
			return true;
		} else if (data.length == 2 && data[0] == ConstantsStk500v1.STK_INSYNC && data[1] == ConstantsStk500v1.STK_NODEVICE) {
			Log.d(TAG, "in sync, no device... what?!?");
			return false;
		} else if (data.length == 1 && data[0] == ConstantsStk500v1.STK_NOSYNC) {
			Log.d(TAG, "no sync");
			return false;
		} else {
			Log.d(TAG, "summin else " + data);
			return false;
		}
	}

	public boolean reset() {
		// reset the arduino
		try {
			mSerialDevice.setDTR(false);
			Thread.sleep(2);
			mSerialDevice.setDTR(true);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void getSynchronization() {
		byte[] getSyncCommand = {ConstantsStk500v1.STK_GET_SYNC, ConstantsStk500v1.CRC_EOP};

		for (int i=0; i<5; i++) {
//			Log.d(TAG, "sending sync command.");
			try {
				mSerialDevice.write(getSyncCommand, 100);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void enterProgrammingMode() {
		byte[] command = new byte[] {ConstantsStk500v1.STK_ENTER_PROGMODE, ConstantsStk500v1.CRC_EOP};
		for (int i=0; i<2; i++) {
			if (waitingForSerialData.get()) {
//				Log.d(TAG, "sending enter programming mode command. try number " + i);
				try {
					mSerialDevice.write(command, 100);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void loadAddress(int writeNumber) {
		int low = 0, high = 0;

		for (int i=0; i<writeNumber; i++) {
			low += 64;
			if (low >= 256) {
				low = 0;
				high++;
			}
		}

		byte[] loadAddress = {ConstantsStk500v1.STK_LOAD_ADDRESS, (byte)low, (byte)high, ConstantsStk500v1.CRC_EOP};
//		Log.d(TAG, "loading address low " + Integer.toHexString(low) + " high " + Integer.toHexString(high) + " combined " + Integer.toHexString(low + high));
		try {
			mSerialDevice.write(loadAddress, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(15);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void programFlash(byte[] data) {
		int chunkSize = 128; // bytes
		// split data up into chunks of 123 bytes (plus 5 bytes overhead, makes 128 bytes)
		int loops = (int)Math.ceil((double)data.length / (chunkSize));

//		Log.d(TAG, "length of data " + data.length);
//		Log.d(TAG, "number of loops " + loops);

		for (int i=0; i<loops; i++) {
//			Log.d(TAG, "loop number " + i);
			// send the number to the address method
			// this tells the bootloader where we want to send the data
			loadAddress(i);

			// move along the byte array in 128 byte chunks
			// 0, 128, 256, etc...
			int start = (i*(chunkSize));

			// end index.  this is the end of the incoming data array we take from
			// 127, 255, etc...
			int end = (i < loops-1) ? (start + (chunkSize-1)) : data.length-1;

			// length of program array
			// get the overall length
			int length = end - start + 1;

//			Log.d(TAG, "start is " + start);
//			Log.d(TAG, "end is " + end);
//			Log.d(TAG, "length is " + length);

			// create a new array the proper length
			byte[] programPage = new byte[length+5];
			// add the beginning overhead
			programPage[0] = ConstantsStk500v1.STK_PROG_PAGE;
			programPage[1] = (byte) ((length >> 8) & 0xFF);
			programPage[2] = (byte) (length & 0xFF);
			// Write flash
			programPage[3] = (byte)'F';

			// Put all the data together with the rest of the command
			// write 4-132.  0-3 is above, 133 is crc below
			// length-overhead is 128-5 = 123
			// this will loop 123 times 0-122
			for (int j = 0; j < length; j++) {
				programPage[j+4] = data[j+start];
			}

			// add crc byte to the end
			programPage[length+5-1] = ConstantsStk500v1.CRC_EOP;

//			Log.d(TAG, "programPage is " + programPage.length + " bytes long");
			try {
				mSerialDevice.write(programPage, 100);
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
//				Log.d(TAG, e.toString());
				e.printStackTrace();
				return;
			}
		}
	}


	private void exitProgrammingMode() {
		byte[] exit = new byte[] {ConstantsStk500v1.STK_LEAVE_PROGMODE, ConstantsStk500v1.CRC_EOP};
		try {
			mSerialDevice.write(exit, 100);
		} catch (IOException e) {
			e.printStackTrace();
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
