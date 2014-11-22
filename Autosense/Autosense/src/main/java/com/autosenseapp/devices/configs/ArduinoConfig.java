package com.autosenseapp.devices.configs;

/**
 * Created by eric on 2014-08-30.
 */
public interface ArduinoConfig {
	public static final int BLACK = -13226195;
	public static final int RED = -65536;
	public static final int GREEN = -16711936;
	public static final int BLUE = -16776961;
	public static final int PINK = -1310580;
	public static final int ORANGE = -551907;
	public static final int YELLOW = -256;

	public void handleClick(int color);
	public void onResume();
}
