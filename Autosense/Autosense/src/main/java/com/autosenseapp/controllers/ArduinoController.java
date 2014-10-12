package com.autosenseapp.controllers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import com.autosenseapp.devices.Arduino;
import com.autosenseapp.devices.Device;
import com.autosenseapp.devices.IdiotLights;
import com.autosenseapp.devices.Master;
import com.autosenseapp.devices.outputTriggers.OnBootTrigger;
import com.autosenseapp.devices.usbInterfaces.ArduinoAccessory;
import com.autosenseapp.devices.usbInterfaces.ArduinoDevice;
import com.autosenseapp.devices.usbInterfaces.ArduinoInterface;
import com.autosenseapp.interfaces.ArduinoListener;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by eric on 2014-10-08.
 */
@Singleton
public class ArduinoController {

	private static final String TAG = ArduinoController.class.getSimpleName();

	public static final String ACCESSORY_READY = "com.autosenseapp.AccessoryReady";
	public static final String ARDUINO_TYPE = "arduino_type";
	public static final int ARDUINO_NONE = 1;
	public static final int ARDUINO_ACCESSORY = 2;
	public static final int ARDUINO_DEVICE = 3;

	private Arduino arduino;
	private ArduinoInterface arduinoInterface;
	@Inject Context context;
	private SparseArray<Device> devices;
	@Inject SharedPreferences sharedPreferences;
	private Thread thread;
	private boolean accessoryReadyBroadcastSent = false;
	private boolean accessoryRunning = false;

	@Inject
	public ArduinoController() { }

	public void onStart(Intent intent) {
		// test if we received an accessory or a device and start the proper mode
		if (intent.hasExtra(UsbManager.EXTRA_ACCESSORY)) {
			Log.d(TAG, "accessory found");
			sharedPreferences.edit().putInt(ARDUINO_TYPE, ARDUINO_ACCESSORY).apply();
			arduinoInterface = new ArduinoAccessory();
		} else if (intent.hasExtra(UsbManager.EXTRA_DEVICE)) {
			Log.d(TAG, "device found");
			sharedPreferences.edit().putInt(ARDUINO_TYPE, ARDUINO_DEVICE).apply();
			arduinoInterface = new ArduinoDevice();
		} else {
			Log.d(TAG, "nothing found");
			sharedPreferences.edit().putInt(ARDUINO_TYPE, ARDUINO_NONE).apply();
		}

		// create the new device
		arduinoInterface.onCreate(context, intent);

		thread = new Thread(null, arduinoRunnable, TAG);
		thread.start();

		arduino = new Arduino();
		// populate the devices array
		populateArduinoWithDevices();
	}

	public void onDestroy() {
		Log.d(TAG, "on destroy");
		accessoryReadyBroadcastSent = false;
		arduinoInterface.onDestroy();
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}

		// tell each sensor to cleanup
		for (int i=0, size = devices.size(); i<size; i++) {
			devices.valueAt(i).cleanUp();
		}
	}

	private void populateArduinoWithDevices() {
		// The device constructor takes a context, the constant that defines the device on the arduino side (just a number) and an intent to fire when data received
		devices = new SparseArray<Device>();
		// populate the devices array with the devices, using their id as the key and the object as the value
		devices.put(Master.id, new Master(context));
		devices.put(IdiotLights.id, new IdiotLights(context));

		// pass in the devices to the arduino.
		// set al devices to use the listener in this class.
		arduino.setDevices(devices, listener);
	}

	public boolean isAccessoryRunning() {
		return accessoryRunning;
	}

	// create a new listener to pass to all the sensors.  they use this to send data out via the arduino
	private ArduinoListener listener = new ArduinoListener() {
		@Override
		public void writeData(Intent intent, int from) {
			// get the bundle from the intent
			Bundle bundle = intent.getExtras();
			// get the command in the bundle
			byte command = bundle.getByte("command");
			// get the values array
			byte values[] = bundle.getByteArray("values");

			// data has "I have this much data, here it is."  It's missing the "hey you, it's me part"
			// create new array three bytes bigger
			byte dataToSend[] = new byte[values.length+5];

			// copy the array, and move up two positions
			dataToSend[0] = (byte)from; // hey you (we need the recipient first)
			dataToSend[1] = Device.id; // it's me
			dataToSend[2] = (byte)(values.length+1); // I have this much data
			dataToSend[3] = command; // this is the command

			// copy the values into the datatosend array
			System.arraycopy(values, 0, dataToSend, 4, values.length);

			// We use this new array to calculate the checksum
			// FIXME.  We need to checksum EVERYTHING not just the data
			// we need to remove the initial length from the data array, or our checksum will be incorrect
			byte checksum[] = new byte[values.length+1];
			// copy the data into a new array to get the checksum
			System.arraycopy(dataToSend, 3, checksum, 0, checksum.length);

			// copy the checksum into our array to be sent over the wire
			dataToSend[dataToSend.length-1] = (byte)getChecksum(from, Device.id, values.length+1, getIntArray(checksum));

			// send the data over the wire
			arduinoInterface.write(dataToSend);
		}
	};

	private Runnable arduinoRunnable = new Runnable() {
		@Override
		public void run() {
			// send the broadcast that we are starting up.  do what needs doing
			context.sendBroadcast(new Intent(OnBootTrigger.receiverString));

			int ret = 0;
			byte[] buffer = new byte[255];

			int recipient;
			int sender;
			int length;
			int checksum;
			while (ret >= 0) {
				accessoryRunning = true;
				try {
					ret = arduinoInterface.read(buffer);
				} catch (Exception e) {
					Log.d(TAG, "stopping");
					accessoryRunning = false;
					e.printStackTrace();
					arduinoInterface.onDestroy();
					thread.interrupt();
					break;
				}

				// send a broadcast once the accessory is connected
				if (!accessoryReadyBroadcastSent) {
					context.sendBroadcast(new Intent(ACCESSORY_READY));
					accessoryReadyBroadcastSent = true;
				}
				// bytes are signed.  we need the unsigned version.  Thus the & 0xFF
				// we don't care about who's receiving.  if it's sent to the master, we inspect it
				try {
					if (buffer[0] == 0x7e) {
						recipient = buffer[1] & 0xFF;
						sender = buffer[2] & 0xFF;
						length = buffer[3];
						// the data starts at position 3, so the end is 3 plus the length.
						byte data[] = Arrays.copyOfRange(buffer, 4, 4 + length);
						// get the checksum
						checksum = buffer[length + 4] & 0xFF;
						// get an int array from the bytes.  this "converts" to our unsigned version
						int dataInt[] = getIntArray(data);
						// if the received checksum equals the calculated checksum, send the data off
						if (checksum == getChecksum(sender, recipient, length, dataInt)) {
							arduino.parseData(sender, length, dataInt, checksum);
							buffer[0] = 0; // reset the first byte
						}
					}
				} catch (IllegalArgumentException e) {
					Log.e(TAG, e.toString());
				}
			}
		}
	};

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
	private int getChecksum(int sender, int receiver, int length, int[] data) {
//		Log.d(TAG, "get checksum");
		int XOR = 0;
		XOR ^= sender;
		XOR ^= receiver;
		XOR ^= length;
		for(int d : data) {
//			Log.d(TAG, "data " + d);
			XOR ^= d;
		}
		return XOR;
	}

}
