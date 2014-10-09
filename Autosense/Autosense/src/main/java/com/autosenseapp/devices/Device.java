package com.autosenseapp.devices;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.autosenseapp.interfaces.ArduinoListener;
import java.lang.reflect.Field;

/**
 * Created by eric on 2013-05-28.
 */
abstract public class Device {
	// Globally used commands
	public static final int GETVALUE = 200;

	private static final String TAG = "lydia device";
	public static final int id = 16; // Our "Android" id for the i2c bus

	private Context context;

	public Device(Context context) {
		this.context = context;
	}

	abstract public void cleanUp();
	abstract public void setListener(ArduinoListener listener);
	abstract public void parseData(int sender, int length, int[] data, int checksum);

	protected void getData(int value) {
		Class <? extends Device> device = this.getClass();
		try {
			Field f = device.getField("WRITE");
			byte values[] = {(byte)value};
			// send out a request for the current value passed.  this ensures our preference and arduino are in sync
			Bundle data = new Bundle();
			data.putByte("command", (byte)Device.GETVALUE);
			data.putByteArray("values", values);
			context.sendBroadcast(new Intent((String)f.get(null)).putExtras(data));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

}
