package ca.efriesen.lydia.devices;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by eric on 2013-05-28.
 */
abstract public class Device {
	private int id;
	// value comes from the serial buffer, it is a string object
	private String value;
	private String intentFilter;
	private Context context;

	private static final String TAG = "lydia device";

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

	public void initialize() {}

	// send a broadcast with the new value we have received
	public void setValue(ArrayList<String> commands) {
		// all arrays should have at least two values, 0 is their command, and 1 is the value
		this.value = commands.get(1);
//		Log.d(TAG, "setting value of " + id + " to " + value);
		context.sendBroadcast(new Intent(intentFilter).putExtra(intentFilter, value));
	}
}
