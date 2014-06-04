package ca.efriesen.lydia.devices;

import android.content.*;
import android.os.Bundle;
import android.util.Log;
import ca.efriesen.lydia.includes.Helpers;
import ca.efriesen.lydia.services.ArduinoService;

/**
 * Created by eric on 2014-05-04.
 */
public class IdiotLights extends Device {

	private static final String TAG = "Lydia idiot lights";

	// the id is the same as the physical devices i2c address
	public static final int id = 12;

	public static final String WRITE = "ca.efriesen.lydia.IdiotlightsWrite";

	// Intent strings
	public static final String IDIOTLIGHTS = "ca.efriesen.lydia.IdiotLights";
	public static final String CURRENTFUEL = "ca.efriesen.lydia.CurrentFuel";
	public static final String CURRENTRPM = "ca.efriesen.lydia.CurrentRPM";
	public static final String CURRENTSPEED = "ca.efriesen.lydia.CurrentSpeed";
	public static final String SPEEDOCALIBRATINGPULSES = "ca.efriesen.lydia.SpeedoCalibratingPulses";

	// idiot light commands
	// These all MUST match what's defined in the Arduino code, or else they won't work
	public static final int BACKLIGHT = 103;
	public static final int BACKLIGHTAUTOBRIGHTNESS = 109;
	public static final int BACKLIGHTBRIGHTNESS = 108;
	public static final int CALIBRATE = 112;
	public static final int FUEL = 100;
	public static final int RPM = 101;
	public static final int SPEED = 102;
	public static final int SPEEDOINPULSES = 110;
	public static final int SPEEDOOUTPULSES = 111;

	// use the context for broadcasts
	// don't use localbroadcast manager because the arduino service runs in it's own process, and it won't work
	private Context context;
	private ArduinoService.ArduinoListener listener;

	public IdiotLights(Context context) {
		super(context);
		this.context = context;
		context.registerReceiver(receiver, new IntentFilter(WRITE));
		context.registerReceiver(accessoryReadyReceiver, new IntentFilter(ArduinoService.ACCESSORY_READY));
	}

	@Override
	public void cleanUp() {
		this.context.unregisterReceiver(receiver);
		this.context.unregisterReceiver(accessoryReadyReceiver);
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
//				Log.d(TAG, "got rpm info: " + rpm);
				context.sendBroadcast(new Intent(IdiotLights.IDIOTLIGHTS).putExtra(CURRENTRPM, String.valueOf(Helpers.word(data[1], data[2]))));
				break;
			}
			case SPEED: {
//				Log.d(TAG, "got speed info: " + speed);
				context.sendBroadcast(new Intent(IdiotLights.IDIOTLIGHTS).putExtra(CURRENTSPEED, String.valueOf(Helpers.word(data[1], data[2]))));
				break;
			}
			case BACKLIGHTBRIGHTNESS: {
				// convert the value received to a range of 0.0-1.0
				float value = data[1] / (float)256;
				// store the value for the slider
				// get the shared prefs here.  this ensures we got an updated copy
				SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
				sharedPreferences.edit().putFloat("backlightBrightness", value).apply();
				break;
			}
			case BACKLIGHTAUTOBRIGHTNESS: {
				// data[1] > 0 returns boolean true/false
				// get the shared prefs here.  this ensures we got an updated copy
				SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
				sharedPreferences.edit().putBoolean("backlightAutoBrightness", data[1] > 0).apply();
				break;
			}
			case SPEEDOINPULSES: {
				int pulses = Helpers.word(data[1], data[2]);
//				Log.d(TAG, "got speedo in pulses " + pulses);
				// get the shared prefs here.  this ensures we got an updated copy
				SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
				sharedPreferences.edit().putString("speedoInputPulses", String.valueOf(pulses)).apply();
				context.sendBroadcast(new Intent(SPEEDOCALIBRATINGPULSES).putExtra("pulses", pulses));
				break;
			}
			case SPEEDOOUTPULSES: {
				Log.d(TAG, "got speedo out pulses " + Helpers.word(data[1], data[2]));
				// get the shared prefs here.  this ensures we got an updated copy
				SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
				sharedPreferences.edit().putString("speedoOutputPulses", String.valueOf(Helpers.word(data[1], data[2]))).apply();
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
			byte values[] = data.getByteArray("values");

			byte d[] = new byte[values.length+2];
			d[0] = (byte)(values.length+1);
			d[1] = command;
			for (int i=0; i<values.length; i++) {
				d[i+2] = values[i];
			}
			write(d);
		}
	};

	// send the data out once the accessory is ready
	private BroadcastReceiver accessoryReadyReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// request some data from the slaves.  ensures out shared prefs are up to date
			getData(IdiotLights.BACKLIGHTAUTOBRIGHTNESS);
			getData(IdiotLights.BACKLIGHTAUTOBRIGHTNESS);
			getData(IdiotLights.SPEEDOINPULSES);
			getData(IdiotLights.SPEEDOOUTPULSES);
		}
	};
}
