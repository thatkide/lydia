package com.autosenseapp.devices;

import android.util.Log;
import android.util.SparseArray;
import com.autosenseapp.services.ArduinoService;

/**
 * Created by eric on 2013-05-28.
 */
public class Arduino {
	// debug tag
	private static final String TAG = Arduino.class.getSimpleName();

	// array list of devices passed in
	private SparseArray<Device> devices = new SparseArray<Device>();

	// take the list of devices from the hardware manager, add them to our own list, and set the listener
	public void setDevices(SparseArray<Device> devices, ArduinoService.ArduinoListener listener) {
		this.devices = devices;
		for (int i=0, size=devices.size(); i<size; i++) {
			devices.valueAt(i).setListener(listener);
		}
	}

	// get the device we have data for, and hand off the info for it to deal with
	public void parseData(int sender, int length, int[] data, int checksum) {
		// get the device that sent the data.  they'll process it and see if it's worth keeping or not
		Device device = devices.get(sender);
		try {
			device.parseData(sender, length, data, checksum);
		} catch (Exception e) {
			Log.d(TAG, "Device missing ", e);
		}
	}
}
