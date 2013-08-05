package ca.efriesen.lydia.devices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import ca.efriesen.lydia.interfaces.SerialIO;
import ca.efriesen.lydia_common.includes.Constants;
import ca.efriesen.lydia_common.includes.Intents;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.nio.ByteBuffer;

/**
 * Created by eric on 2013-05-28.
 */
public class TemperatureSensor extends Device implements SerialIO {
	private Context context;
	private SerialInputOutputManager serialInputOutputManager = null;

	public TemperatureSensor(Context context, int id, String intentFilter) {
		super(context, id, intentFilter);
		this.context = context;
		context.registerReceiver(getTemperatureReceiver, new IntentFilter(Intents.GETTEMPERATURE));
	}

	@Override
	public void cleanUp() {
		context.unregisterReceiver(getTemperatureReceiver);
	}

	public BroadcastReceiver getTemperatureReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				write(ByteBuffer.allocate(4).putInt(Constants.GETTEMPERATURE).array());
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public void setIOManager(Object serialInputOutputManager) {
		this.serialInputOutputManager = (SerialInputOutputManager) serialInputOutputManager;
	}

	@Override
	public void write(byte[] command) {
		// write the bytes to the arduino
		serialInputOutputManager.writeAsync(command);
	}
}