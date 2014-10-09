package com.autosenseapp.interfaces;

import android.content.Intent;

/**
 * Created by eric on 2014-10-08.
 */
// new listener interface.
// provide both byte array and int methods.
public interface ArduinoListener {
	public void writeData(Intent intent, int from);
}
