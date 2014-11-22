package com.autosenseapp.devices.usbInterfaces;

import android.content.Context;
import android.content.Intent;
import java.io.IOException;

/**
 * Created by eric on 2014-08-26.
 */
public interface ArduinoInterface {

	public void onCreate(Context context, Intent intent);
	public void onDestroy();
	public int read(byte[] buffer) throws IOException;
	public void write(byte[] data);
}
