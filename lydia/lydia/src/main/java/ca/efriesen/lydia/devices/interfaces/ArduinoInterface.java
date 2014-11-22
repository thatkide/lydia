package ca.efriesen.lydia.devices.interfaces;

import android.content.Context;
import android.content.Intent;

/**
 * Created by eric on 2014-08-23.
 */
public abstract class ArduinoInterface {

	protected Context context;

	public ArduinoInterface(Context context) {
		this.context = context;
	}

	public abstract void onStart(Intent intent);

	public abstract void close();
	public abstract int read(byte[] buffer);
	public abstract void write(byte[] data);

}
