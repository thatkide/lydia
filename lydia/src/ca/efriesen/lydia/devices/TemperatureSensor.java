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
public class TemperatureSensor extends Device {
	private Context context;
	private String intentFilter;
	private ArduinoService.ArduinoListener listener;

	public TemperatureSensor(Context context, String intentFilter) {
		this.context = context;
		this.intentFilter = intentFilter;
		context.registerReceiver(getTemperatureReceiver, new IntentFilter(Intents.GETTEMPERATURE));
	}

	@Override
	public void cleanUp() {
		context.unregisterReceiver(getTemperatureReceiver);
	}

	@Override
	public void setListener(ArduinoService.ArduinoListener listener) {
		this.listener = listener;
	}


	@Override
	public void parseData(int sender, int length, int[] data, int checksum) {
		int temp = ((data[0] << 8) | (data[1] & 0xFF));
		context.sendBroadcast(new Intent(intentFilter).putExtra(intentFilter, String.valueOf(temp)));
	}

	@Override
	public void write(byte[] data) {

	}

	public BroadcastReceiver getTemperatureReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		//	listener.writeData(Constants.GETTEMPERATURE);
		}
	};
}