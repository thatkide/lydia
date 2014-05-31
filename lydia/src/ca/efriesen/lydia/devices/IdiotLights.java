package ca.efriesen.lydia.devices;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ca.efriesen.lydia.services.ArduinoService;

/**
 * Created by eric on 2014-05-04.
 */
public class IdiotLights extends Device {

	private static final String TAG = "Lydia idiot lights";

	// the id is the same as the physical devices i2c address
	public static final int id = 12;

	public static final String IDIOTLIGHTSWRITE = "ca.efriesen.lydia.IdiotLightsWrite";

	// Intent strings
	public static final String IDIOTLIGHTS = "ca.efriesen.lydia.IdiotLights";
	public static final String CURRENTFUEL = "ca.efriesen.lydia.CurrentFuel";
	public static final String CURRENTRPM = "ca.efriesen.lydia.CurrentRPM";
	public static final String CURRENTSPEED = "ca.efriesen.lydia.CurrentSpeed";

	// idiot light commands
	public static final int FUEL = 100;
	public static final int RPM = 101;
	public static final int SPEED = 102;
	public static final int BACKLIGHT = 103;


	// use the context for broadcasts
	// don't use localbroadcast manager because the arduino service runs in it's own process, and it won't work
	private Context context;
	private ArduinoService.ArduinoListener listener;

	public IdiotLights(Context context) {
		this.context = context;
		context.registerReceiver(receiver, new IntentFilter(IDIOTLIGHTSWRITE));
	}

	@Override
	public void cleanUp() {
		this.context.unregisterReceiver(receiver);
	}

	@Override
	public void setListener(ArduinoService.ArduinoListener listener) {
		this.listener = listener;
	}

	@Override
	public void parseData(int sender, int length, int[] data, int checksum) {
		int command = data[0];
		switch (command) {
			case FUEL: {
				int fuel = data[1];
//				Log.d(TAG, "got fuel info: " + fuel);
				context.sendBroadcast(new Intent(IdiotLights.IDIOTLIGHTS).putExtra(CURRENTFUEL, String.valueOf(fuel)));
				break;
			}
			case RPM: {
				// make a "word" from the two bytes.  Shift the first one over 8 bits, and combine it with the second
				int rpm = ((data[1] << 8) | (data[2] & 0xFF));
//				Log.d(TAG, "got rpm info: " + rpm);
				context.sendBroadcast(new Intent(IdiotLights.IDIOTLIGHTS).putExtra(CURRENTRPM, String.valueOf(rpm)));
				break;
			}
			case SPEED: {
				// make a "word" from the two bytes.  Shift the first one over 8 bits, and combine it with the second
				int speed = ((data[1] << 8) | (data[2] & 0xFF));
//				Log.d(TAG, "got speed info: " + speed);
				context.sendBroadcast(new Intent(IdiotLights.IDIOTLIGHTS).putExtra(CURRENTSPEED, String.valueOf(speed)));
				break;
			}
		}
	}

	@Override
	public void write(byte[] data) {
		listener.writeData(data, id);
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle data = intent.getExtras();
			byte command = data.getByte("command");
			byte value = data.getByte("value");

			byte d[] = {2, command, value};
			write(d);
		}
	};
}
