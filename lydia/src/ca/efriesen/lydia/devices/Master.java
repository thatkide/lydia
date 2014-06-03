package ca.efriesen.lydia.devices;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import ca.efriesen.lydia.services.ArduinoService;

/**
 * Created by eric on 2014-05-04.
 */
public class Master extends Device {

	private static final String TAG = "Lydia master";

	public static final int id = 10;

	public static final int LIGHTLEVEL = 176;

	private Context context;
	private ArduinoService.ArduinoListener listener;

	public Master(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void setListener(ArduinoService.ArduinoListener listener) {
		this.listener = listener;
	}

	@Override
	public void parseData(int sender, int length, int[] data, int checksum) {
		int command = data[0];
		switch (command) {
			case LIGHTLEVEL: {
				int lightLevel = data[1];
				Log.d(TAG, "got light level: " + lightLevel);
//				context.sendBroadcast(new Intent(IdiotLights.IDIOTLIGHTS).putExtra(CURRENTFUEL, String.valueOf(fuel)));
				break;
			}
		}
	}

	@Override
	public void write(byte[] data) {
		listener.writeData(data, id);
	}

}
