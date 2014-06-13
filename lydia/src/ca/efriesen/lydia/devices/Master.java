package ca.efriesen.lydia.devices;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import ca.efriesen.lydia.includes.Helpers;
import ca.efriesen.lydia.services.ArduinoService;

/**
 * Created by eric on 2014-05-04.
 */
public class Master extends Device {

	private static final String TAG = "Lydia master";

	public static final int id = 10;

	public static final String INSIDETEMPERATURE = "ca.efriesen.lydia.INSIDETEMPERATURE";
	public static final String LIGHTVALUE = "ca.efriesen.lydia.LIGHTVALUE";
	public static final String OUTSIDETEMPERATURE = "ca.efriesen.lydia.OUTSIDETEMPERATURE";

	public static final int INSIDETEMP = 160;
	public static final int LIGHTLEVEL = 176;
	public static final int OUTSIDETEMP = 161;

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
			case INSIDETEMP: {
				int insideTemp = Helpers.word(data[1], data[2]);
				context.sendBroadcast(new Intent(INSIDETEMPERATURE).putExtra(INSIDETEMPERATURE, String.valueOf(insideTemp)));
				Log.d(TAG, "got inside temp: " + insideTemp);
				break;
			}
			case LIGHTLEVEL: {
				int lightLevel = Helpers.word(data[1], data[2]);
//				Log.d(TAG, "got light level: " + lightLevel);
				context.sendBroadcast(new Intent(LIGHTVALUE).putExtra(LIGHTVALUE, String.valueOf(lightLevel)));
				break;
			}
			case OUTSIDETEMP: {
				int outsideTemp = Helpers.word(data[1], data[2]);
				context.sendBroadcast(new Intent(OUTSIDETEMPERATURE).putExtra(OUTSIDETEMPERATURE, String.valueOf(outsideTemp)));
//				Log.d(TAG, "got outside temp: " + outsideTemp);
				break;
			}
		}
	}


}
