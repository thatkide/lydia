package ca.efriesen.lydia.devices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import ca.efriesen.lydia.interfaces.SerialIO;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.nio.ByteBuffer;

/**
 * Created by eric on 2013-05-28.
 */
public class Seats extends Device implements SerialIO {
	private static final String TAG = "seats";

	private Context context;
	private SerialInputOutputManager serialInputOutputManager = null;

	public Seats(Context context, int id, String intentFilter) {
		super(context, id, intentFilter);
		this.context = context;

		context.registerReceiver(seatsReceiver, new IntentFilter(intentFilter));
	}

	@Override
	public void cleanUp() {
		try {
			context.unregisterReceiver(seatsReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver seatsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int seatId = intent.getIntExtra("seatId", 0);
			int temperature = intent.getIntExtra("temp", 0);

			// is this driver or passenger seat0
			if (seatId == getId()) {
				Log.d(TAG, "setting seat " + getId() + " to " + temperature);
				// convert the int to a byte array
				write(ByteBuffer.allocate(4).putInt(getId()).array());
				// convert the int to a byte array
				write(ByteBuffer.allocate(4).putInt(temperature).array());
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
			return;
		}
		// write the bytes to the arduino
		serialInputOutputManager.writeAsync(command);
	}
}
