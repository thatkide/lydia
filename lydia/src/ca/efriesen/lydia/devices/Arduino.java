package ca.efriesen.lydia.devices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.util.Log;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.includes.ConstantsStk500v1;
import ca.efriesen.lydia.includes.Hex;
import ca.efriesen.lydia.includes.HexFileParser;
import ca.efriesen.lydia.interfaces.SerialIO;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import java.io.*;
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

	// listener for new data
	private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() {
		@Override
		public void onNewData(final byte[] data) {
			// do this part if we're not upgrading the firmware (look for out serial data format)
			if (!upgradingFirmware.get()) {
				// pass new data to our parse method
				parseSerialData(data);
			} else {
				Log.d(TAG, "got new data. length " + data.length);
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

		Log.d(TAG, " -------------------");
		Log.d(TAG, "data: " + serialData.toString());
		Log.d(TAG, "added: " + new String(data).trim());

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

	public void upgradeFirmware() {
		upgradingFirmware.getAndSet(true);
		upgradeThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "staring upload");
				//blink sketch
				String hexData =
//						"3A100000000C9461000C947E000C947E000C947E0095" +
//								"3A100010000C947E000C947E000C947E000C947E0068" +
//								"3A100020000C947E000C947E000C947E000C947E0058" +
//								"3A100030000C947E000C947E000C947E000C947E0048" +
//								"3A100040000C949A000C947E000C947E000C947E001C" +
//								"3A100050000C947E000C947E000C947E000C947E0028" +
//								"3A100060000C947E000C947E00000000002400270009" +
//								"3A100070002A0000000000250028002B0000000000DE" +
//								"3A1000800023002600290004040404040404040202DA" +
//								"3A100090000202020203030303030301020408102007" +
//								"3A1000A0004080010204081020010204081020000012" +
//								"3A1000B0000007000201000003040600000000000029" +
//								"3A1000C000000011241FBECFEFD8E0DEBFCDBF11E08E" +
//								"3A1000D000A0E0B1E0EAEFF3E002C005900D92A0309D" +
//								"3A1000E000B107D9F711E0A0E0B1E001C01D92A9303D" +
//								"3A1000F000B107E1F70E94F4010C94FB010C9400009D" +
//								"3A100100008DE061E00E949C0168EE73E080E090E089" +
//								"3A100110000E94E2008DE060E00E949C0168EE73E0C6" +
//								"3A1001200080E090E00E94E20008958DE061E00E948E" +
//								"3A10013000760108951F920F920FB60F9211242F93FC" +
//								"3A100140003F938F939F93AF93BF93809104019091BE" +
//								"3A100150000501A0910601B0910701309108010196B7" +
//								"3A10016000A11DB11D232F2D5F2D3720F02D57019696" +
//								"3A10017000A11DB11D209308018093040190930501F6" +
//								"3A10018000A0930601B09307018091000190910101B5" +
//								"3A10019000A0910201B09103010196A11DB11D8093B0" +
//								"3A1001A000000190930101A0930201B0930301BF915C" +
//								"3A1001B000AF919F918F913F912F910F900FBE0F9014" +
//								"3A1001C0001F9018959B01AC017FB7F89480910001B6" +
//								"3A1001D00090910101A0910201B091030166B5A89B25" +
//								"3A1001E00005C06F3F19F00196A11DB11D7FBFBA2F49" +
//								"3A1001F000A92F982F8827860F911DA11DB11D62E0A0" +
//								"3A10020000880F991FAA1FBB1F6A95D1F7BC012DC08B" +
//								"3A10021000FFB7F8948091000190910101A091020133" +
//								"3A10022000B0910301E6B5A89B05C0EF3F19F0019618" +
//								"3A10023000A11DB11DFFBFBA2FA92F982F88278E0FA0" +
//								"3A10024000911DA11DB11DE2E0880F991FAA1FBB1FC0" +
//								"3A10025000EA95D1F7861B970B885E9340C8F2215030" +
//								"3A1002600030404040504068517C4F211531054105D8" +
//								"3A10027000510571F60895789484B5826084BD84B583" +
//								"3A10028000816084BD85B5826085BD85B5816085BD91" +
//								"3A10029000EEE6F0E0808181608083E1E8F0E01082AA" +
//								"3A1002A000808182608083808181608083E0E8F0E0EB" +
//								"3A1002B000808181608083E1EBF0E0808184608083D5" +
//								"3A1002C000E0EBF0E0808181608083EAE7F0E080810C" +
//								"3A1002D000846080838081826080838081816080836C" +
//								"3A1002E0008081806880831092C1000895482F50E07B" +
//								"3A1002F000CA0186569F4FFC0124914A575F4FFA016D" +
//								"3A1003000084918823C1F0E82FF0E0EE0FFF1FE85939" +
//								"3A10031000FF4FA591B491662341F49FB7F8948C9157" +
//								"3A10032000209582238C939FBF08959FB7F8948C915A" +
//								"3A10033000822B8C939FBF0895482F50E0CA018255AD" +
//								"3A100340009F4FFC012491CA0186569F4FFC013491B6" +
//								"3A100350004A575F4FFA019491992309F444C022232C" +
//								"3A1003600051F1233071F0243028F42130A1F02230F3" +
//								"3A1003700011F514C02630B1F02730C1F02430D9F483" +
//								"3A1003800004C0809180008F7703C0809180008F7DB2" +
//								"3A100390008093800010C084B58F7702C084B58F7DB4" +
//								"3A1003A00084BD09C08091B0008F7703C08091B000F8" +
//								"3A1003B0008F7D8093B000E92FF0E0EE0FFF1FEE5825" +
//								"3A1003C000FF4FA591B491662341F49FB7F8948C91A7" +
//								"3A1003D000309583238C939FBF08959FB7F8948C9199" +
//								"3A1003E000832B8C939FBF08950E943B010E94950030" +
//								"3A0A03F0000E948000FDCFF894FFCFBB" +
//								"3A00000001FF";
						"3A100000000C9461000C947E000C947E000C947E0095" +
								"3A100010000C947E000C947E000C947E000C947E0068" +
								"3A100020000C947E000C947E000C947E000C947E0058" +
								"3A100030000C947E000C947E000C947E000C947E0048" +
								"3A100040000C94A9000C947E000C947E000C947E000D" +
								"3A100050000C947E000C947E000C947E000C947E0028" +
								"3A100060000C947E000C947E00000000002400270009" +
								"3A100070002A0000000000250028002B0000000000DE" +
								"3A1000800023002600290004040404040404040202DA" +
								"3A100090000202020203030303030301020408102007" +
								"3A1000A0004080010204081020010204081020000012" +
								"3A1000B0000007000201000003040600000000000029" +
								"3A1000C000000011241FBECFEFD8E0DEBFCDBF11E08E" +
								"3A1000D000A0E0B1E0E4E3F4E002C005900D92A030AE" +
								"3A1000E000B107D9F711E0A0E0B1E001C01D92A9303D" +
								"3A1000F000B107E1F70E949A000C9418020C940000DA" +
								"3A100100008DE061E00E94C40168EE73E080E090E061" +
								"3A100110000E94F1008DE060E00E94C40164EF71E094" +
								"3A1001200080E090E00E94F10008958DE061E00E947F" +
								"3A1001300085010895CF93DF930E944A010E949500A4" +
								"3A10014000C0E0D0E00E9480002097E1F30E94000010" +
								"3A10015000F9CF1F920F920FB60F9211242F933F9356" +
								"3A100160008F939F93AF93BF9380910401909105016A" +
								"3A10017000A0910601B0910701309108010196A11DDF" +
								"3A10018000B11D232F2D5F2D3720F02D570196A11D76" +
								"3A10019000B11D209308018093040190930501A09361" +
								"3A1001A0000601B09307018091000190910101A09197" +
								"3A1001B0000201B09103010196A11DB11D80930001C0" +
								"3A1001C00090930101A0930201B0930301BF91AF91FD" +
								"3A1001D0009F918F913F912F910F900FBE0F901F9085" +
								"3A1001E00018959B01AC017FB7F89480910001909124" +
								"3A1001F0000101A0910201B091030166B5A89B05C061" +
								"3A100200006F3F19F00196A11DB11D7FBFBA2FA92F15" +
								"3A10021000982F8827860F911DA11DB11D62E0880FC0" +
								"3A10022000991FAA1FBB1F6A95D1F7BC012DC0FFB74C" +
								"3A10023000F8948091000190910101A0910201B09188" +
								"3A100240000301E6B5A89B05C0EF3F19F00196A11D7B" +
								"3A10025000B11DFFBFBA2FA92F982F88278E0F911D90" +
								"3A10026000A11DB11DE2E0880F991FAA1FBB1FEA95CF" +
								"3A10027000D1F7861B970B885E9340C8F2215030401F" +
								"3A100280004040504068517C4F2115310541055105D2" +
								"3A1002900071F60895789484B5826084BD84B58160D8" +
								"3A1002A00084BD85B5826085BD85B5816085BDEEE67E" +
								"3A1002B000F0E0808181608083E1E8F0E0108280815D" +
								"3A1002C00082608083808181608083E0E8F0E08081CB" +
								"3A1002D00081608083E1EBF0E0808184608083E0EBEB" +
								"3A1002E000F0E0808181608083EAE7F0E080818460D3" +
								"3A1002F000808380818260808380818160808380812F" +
								"3A10030000806880831092C1000895CF93DF93482FB7" +
								"3A1003100050E0CA0186569F4FFC0134914A575F4F07" +
								"3A10032000FA018491882369F190E0880F991FFC01FC" +
								"3A10033000E859FF4FA591B491FC01EE58FF4FC591CC" +
								"3A10034000D491662351F42FB7F8948C91932F909504" +
								"3A1003500089238C93888189230BC0623061F42FB785" +
								"3A10036000F8948C91932F909589238C938881832B7B" +
								"3A1003700088832FBF06C09FB7F8948C91832B8C93F2" +
								"3A100380009FBFDF91CF910895482F50E0CA01825559" +
								"3A100390009F4FFC012491CA0186569F4FFC01949106" +
								"3A1003A0004A575F4FFA013491332309F440C02223A6" +
								"3A1003B00051F1233071F0243028F42130A1F02230A3" +
								"3A1003C00011F514C02630B1F02730C1F02430D9F433" +
								"3A1003D00004C0809180008F7703C0809180008F7D62" +
								"3A1003E0008093800010C084B58F7702C084B58F7D64" +
								"3A1003F00084BD09C08091B0008F7703C08091B000A8" +
								"3A100400008F7D8093B000E32FF0E0EE0FFF1FEE58DA" +
								"3A10041000FF4FA591B4912FB7F894662321F48C91E6" +
								"3A100420009095892302C08C91892B8C932FBF0895BE" +
								"3A04043000F894FFCF6E" +
								"3A00000001FF";


				byte[] data = new byte[hexData.length()/2];
				for (int i=0; i<hexData.length(); i+=2) {
					data[i/2] = (byte) ((Character.digit(hexData.charAt(i), 16) << 4) + Character.digit(hexData.charAt(i+1), 16));
				}

				Hex hex = new Hex(data);


				// reset and then try to sync
				reset();

				waitingForSerialData.getAndSet(true);
				getSynchronization();
				// do nothing while waiting.  the async nature makes things difficult
				while (waitingForSerialData.get()) {}

				synchronized (upgradingInput) {
					// check if we got valid input
					if (!checkInput(upgradingInput)) {
						Log.d(TAG, "input is invalid after get sync");
						return;
					}
				}
				// do nothing until we're in sync
				Log.d(TAG, "we're synced... go baby go");
				waitingForSerialData.getAndSet(true);
				enterProgrammingMode();
				programFlash(hex.getHexLine(0, data.length));
				exitProgrammingMode();

				Log.d(TAG, "we're done, restart");
				// we're done, reset
				reset();
				upgradingFirmware.getAndSet(false);
			}
		});
		upgradeThread.start();
	}

	private boolean checkInput(byte[] data) {
		Log.d(TAG, "received data " + data);
		for (int i=0; i<data.length; i++) {
			Log.d(TAG, "index " + i + " is " + Integer.toHexString(data[i]));
		}
		if (data[0] == ConstantsStk500v1.STK_INSYNC && data[1] == ConstantsStk500v1.STK_OK) {
			Log.d(TAG, "IN SYNC");
			return true;
		} else if (data[0] == ConstantsStk500v1.STK_INSYNC && data[1] == ConstantsStk500v1.STK_NODEVICE) {
			Log.d(TAG, "in sync, no device... what?!?");
			return false;
		} else if (data[0] == ConstantsStk500v1.STK_NOSYNC) {
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
			Log.d(TAG, "sending sync command.");
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
				Log.d(TAG, "sending enter programming mode command. try number " + i);
				try {
					mSerialDevice.write(command, 100);
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
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
		Log.d(TAG, "loading address low " + Integer.toHexString(low) + " high " + Integer.toHexString(high) + " combined " + Integer.toHexString(low + high));
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

		Log.d(TAG, "length of data " + data.length);
		Log.d(TAG, "number of loops " + loops);

		for (int i=0; i<loops; i++) {
			Log.d(TAG, "loop number " + i);
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

			Log.d(TAG, "start is " + start);
			Log.d(TAG, "end is " + end);
			Log.d(TAG, "length is " + length);

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

			Log.d(TAG, "programPage is " + programPage.length + " bytes long");
			try {
				mSerialDevice.write(programPage, 100);
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				Log.d(TAG, e.toString());
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
