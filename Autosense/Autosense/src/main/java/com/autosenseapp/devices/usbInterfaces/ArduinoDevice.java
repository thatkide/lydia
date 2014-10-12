package com.autosenseapp.devices.usbInterfaces;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import com.autosenseapp.AutosenseApplication;
import com.autosenseapp.includes.ConstantsStk500v1;
import com.autosenseapp.includes.Hex;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;

/**
 * Created by eric on 2014-08-26.
 */
public class ArduinoDevice implements ArduinoInterface {

	private static final String TAG = ArduinoDevice.class.getSimpleName();

	public static final String UPGRADE_FIRMWARE = "upgradeFirmware";

	private Context context;
	private UsbSerialPort port;
	@Inject UsbManager usbManager;
	@Inject	SharedPreferences sharedPreferences;

	// upgrading firmware stuff
	private AtomicBoolean upgradingFirmware = new AtomicBoolean();
	private volatile byte[] upgradingInput;
	private AtomicBoolean waitingForSerialData = new AtomicBoolean();
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private SerialInputOutputManager serialInputOutputManager;

	private final SerialInputOutputManager.Listener listener = new SerialInputOutputManager.Listener() {
		@Override
		public void onNewData(byte[] data) {
			upgradingInput = new byte[data.length];
			upgradingInput = data;
			waitingForSerialData.getAndSet(false);
		}

		@Override
		public void onRunError(Exception e) {	}
	};

	@Override
	public void onCreate(Context context, Intent intent) {
		this.context = context;
		((AutosenseApplication)context.getApplicationContext()).inject(this);
		UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

		List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);

		UsbSerialDriver driver = drivers.get(0);

		if (driver.getDevice().getDeviceId() == usbDevice.getDeviceId()) {
			UsbDeviceConnection connection = usbManager.openDevice(usbDevice);

			if (connection == null) {
				// error
				Log.e(TAG, "no connection");
			}

			port = driver.getPorts().get(0);
			try {
//				Log.d(TAG, "device open");
				port.open(connection);
				port.setParameters(115200, 8, 1, 0);
				if (sharedPreferences.getBoolean("autoUpgradeFirmware", true)) {
					checkFirmware();
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		serialInputOutputManager = new SerialInputOutputManager(port, listener);

		context.registerReceiver(updateReceiver, new IntentFilter(UPGRADE_FIRMWARE));
	}

	@Override
	public void onDestroy() {
		if (!upgradingFirmware.get()) {
			try {
				port.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
		try {
			context.unregisterReceiver(updateReceiver);
		} catch (Exception e) {}
	}

	private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			checkFirmware(true);
		}
	};

	@Override
	public int read(byte[] buffer) throws IOException {
		if (!upgradingFirmware.get()) {
			return port.read(buffer, 500);
		}
		return 1;
	}

	@Override
	public void write(byte[] data) {
		if (!upgradingFirmware.get()) {
			try {
				// add a delimiter for the serial comms
				byte[] del = {(byte) 0x7e};
				byte[] output = new byte[data.length + del.length];
				System.arraycopy(data, 0, output, 0, data.length);
				System.arraycopy(del, 0, output, data.length, del.length);
				port.write(output, 500);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}

	public void checkFirmware() {
		checkFirmware(false);
	}

	public void checkFirmware(boolean force) {
		executorService.submit(serialInputOutputManager);

		try {
			// start a new md5 digest
			MessageDigest digester = MessageDigest.getInstance("MD5");
			// open the hex file
			InputStream inputStream = context.getAssets().open("master.hex");
			// create a new byte array the proper length
			byte[] bytes = new byte[inputStream.available()];
			int byteCount;
			// read the stream and update the digester
//			Log.d(TAG, "read input stream");
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
//			Log.d(TAG, "digest " + hexString.toString());
//			Log.d(TAG, "storing in pref");
			// store the hash in shared pref
			sharedPreferences.edit().putString("currentFirmwareDigest", hexString.toString()).apply();
			// get the previous hash
			String previousFirmware = sharedPreferences.getString("previousFirmwareDigest", "");
//			Log.d(TAG, "get prev pref " + previousFirmware);
			// compare them, if they don't equal, update the firmware
			if (!previousFirmware.equalsIgnoreCase(hexString.toString()) || previousFirmware.equalsIgnoreCase("") || force) {
//				Log.d(TAG, "do upgrade");
				sharedPreferences.edit().putString("previousFirmwareDigest", hexString.toString()).apply();
				upgradeFirmware();
			}

		} catch (NoSuchAlgorithmException e) {
//			Log.e(TAG, "no such digest");
		} catch (IOException e) {
//			Log.d(TAG, e.toString());
		}
//		Log.d(TAG, "check complete");
	}

	public void upgradeFirmware() {
		upgradingFirmware.getAndSet(true);
		Thread upgradeThread = new Thread(new Runnable() {
			@Override
			public void run() {
//				Log.d(TAG, "staring upload");
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
//					Log.d(TAG, "input is invalid after get sync");
					return;
				}
				// enter programming mode
				enterProgrammingMode();
				// program the flash in the arduino, getting the hex data from the parser
				programFlash(hexData);
				// exit programming mode, we're done
				exitProgrammingMode();
				// we're done, reset
				reset();
				upgradingFirmware.set(false);
			}
		});
		upgradeThread.start();
	}

	private boolean checkInput(byte[] data) {
//		Log.d(TAG, "received data " + data);
		for (int i=0; i<data.length; i++) {
//			Log.d(TAG, "index " + i + " is " + Integer.toHexString(data[i]));
		}
		if (data.length == 2 && data[0] == ConstantsStk500v1.STK_INSYNC && data[1] == ConstantsStk500v1.STK_OK) {
//			Log.d(TAG, "IN SYNC");
			return true;
		} else if (data.length == 2 && data[0] == ConstantsStk500v1.STK_INSYNC && data[1] == ConstantsStk500v1.STK_NODEVICE) {
//			Log.d(TAG, "in sync, no device... what?!?");
			return false;
		} else if (data.length == 1 && data[0] == ConstantsStk500v1.STK_NOSYNC) {
//			Log.d(TAG, "no sync");
			return false;
		} else {
//			Log.d(TAG, "summin else " + data);
			return false;
		}
	}

	public boolean reset() {
		// reset the arduino
		try {
			port.setDTR(false);
			Thread.sleep(2);
			port.setDTR(true);
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
				port.write(getSyncCommand, 100);
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
					port.write(command, 100);
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
			port.write(loadAddress, 100);
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
				port.write(programPage, 100);
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
			port.write(exit, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}
		serialInputOutputManager.stop();
	}
}
