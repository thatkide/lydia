package ca.efriesen.lydia.devices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import ca.efriesen.lydia.interfaces.SerialIO;
import ca.efriesen.lydia_common.includes.Constants;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.nio.ByteBuffer;

/**
 * Created by eric on 2013-05-28.
 */
public class Wipers extends Device implements SerialIO {
	private static final String TAG = "wipers";

	private Context context;
	private SerialInputOutputManager serialInputOutputManager = null;

	public Wipers(Context context, int id, String intentFilter) {
		super(context, id, intentFilter);
		this.context = context;

		context.registerReceiver(wipersReceiver, new IntentFilter(intentFilter));
	}

	@Override
	public void cleanUp() {
		try {
			context.unregisterReceiver(wipersReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver wipersReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// initialize with a 0 state
			byte[] value = {0};
			Log.d(TAG, "setting wipers");

			// convert the window defroster constant into a byte array
			write(ByteBuffer.allocate(4).putInt(Constants.WIPE).array());
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