package ca.efriesen.lydia.devices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import ca.efriesen.lydia.services.ArduinoService;
import ca.efriesen.lydia_common.includes.Constants;
import ca.efriesen.lydia_common.includes.Intents;


/**
 * Created by eric on 2013-05-28.
 */
public class LightSensor extends Device{
	private Context context;
	private ArduinoService.ArduinoListener listener;

	public LightSensor(Context context) {
		this.context = context;
		context.registerReceiver(getLightValueReceiver, new IntentFilter(Intents.LIGHTVALUE));
	}

	@Override
	public void cleanUp() {
		context.unregisterReceiver(getLightValueReceiver);
	}

	@Override
	public void setListener(ArduinoService.ArduinoListener listener) {
		this.listener = listener;
	}

	@Override
	public void parseData(int sender, int length, int[] data, int checksum) {

	}

	@Override
	public void write(byte[] data) {

	}

	public BroadcastReceiver getLightValueReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
//			listener.writeData(Constants.GETLIGHT);
		}
	};

}
