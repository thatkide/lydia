package ca.efriesen.lydia.devices;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by eric on 2013-05-28.
 */
abstract public class Device {
	private int id;
	// value comes from the serial buffer, it is a string object
	private String value;
	private String intentFilter;
	private Context context;

	private static final String TAG = "hardware";

	public Device(Context context, int id, String intentFilter) {
		this.context = context;
		this.id = id;
		this.intentFilter = intentFilter;
	}

	abstract public void cleanUp();

	public int getId() {
		return this.id;
	}

	public int getIntValue() {
		return Integer.valueOf(value);
	}

	public String getStringValue() {
		return value;
	}

	// send a broadcast with the new value we have received
	public void setValue(String value) {
		this.value = value;
//		Log.d(TAG, "setting value of " + id + " to " + value);
		context.sendBroadcast(new Intent(intentFilter).putExtra(intentFilter, value));
	}
}
