package ca.efriesen.lydia.devices;

import ca.efriesen.lydia.services.ArduinoService;

/**
 * Created by eric on 2013-05-28.
 */
abstract public class Device {
	private static final String TAG = "lydia device";

	public static final int id = 16; // Our "Android" id for the i2c bus

	abstract public void cleanUp();
	abstract public void setListener(ArduinoService.ArduinoListener listener);
	abstract public void parseData(int sender, int length, int[] data, int checksum);
	abstract public void write(byte[] data);
}
