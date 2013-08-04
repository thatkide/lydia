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
public class LightSensor extends Device implements SerialIO{
	private Context context;
	private SerialInputOutputManager serialInputOutputManager = null;

	public LightSensor(Context context, int id, String intentFilter) {
		super(context, id, intentFilter);
		this.context = context;
		context.registerReceiver(getLightValueReceiver, new IntentFilter(Intents.LIGHTVALUE));
	}

	@Override
	public void cleanUp() {
		context.unregisterReceiver(getLightValueReceiver);
	}

	public BroadcastReceiver getLightValueReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				write(ByteBuffer.allocate(4).putInt(Constants.GETLIGHT).array());
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
		if (serialInputOutputManager == null) {
			throw new NullPointerException("Serial IO Manager is null");
		}
		// write the bytes to the arduino
		serialInputOutputManager.writeAsync(command);

	}
}
