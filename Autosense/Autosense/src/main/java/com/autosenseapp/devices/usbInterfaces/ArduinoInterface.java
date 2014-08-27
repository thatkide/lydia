package com.autosenseapp.devices.usbInterfaces;

import android.content.Context;
import android.content.Intent;

/**
 * Created by eric on 2014-08-26.
 */
public interface ArduinoInterface {

	public void onCreate(Context context, Intent intent);
	public void onDestroy();
	public int read(byte[] buffer);
	public void write(byte[] data);
}
