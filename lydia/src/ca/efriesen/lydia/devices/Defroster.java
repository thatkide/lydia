package ca.efriesen.lydia.devices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import ca.efriesen.lydia.interfaces.SerialIO;
import ca.efriesen.lydia_common.includes.Constants;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.nio.ByteBuffer;

/**
 * Created by eric on 2013-05-28.
 */
public class Defroster extends Device implements SerialIO {
	private static final String TAG = "defroster";

	// application context.  we need this for the broadcast receiver
	private Context context;
	private SerialInputOutputManager serialInputOutputManager = null;

	// constructor.  get the required info and pass it to the super class
	public Defroster(Context context, int id, String intentFilter) {
		super(context, id, intentFilter);
		this.context = context;

		// register the broadcast receiver
		context.registerReceiver(defrosterReceiver, new IntentFilter(intentFilter));
	}

	// here we do cleanup tasks, such as unregistering the broadcast receiver
	@Override
	public void cleanUp() {
		try {
			context.unregisterReceiver(defrosterReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// this is broadcast from the ui controls
	private BroadcastReceiver defrosterReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// initialize with a 0 state
			byte[] value = {0};

			// convert the window defroster constant into a byte array, and write it to the serial bus
			write(ByteBuffer.allocate(4).putInt(Constants.REARWINDOWDEFROSTER).array());

			// set the value of [0] to either 0 or 1, depending on if getbooleanextra is true or false
			value[0] = (intent.getBooleanExtra("state", false) ? (byte)1 : (byte)0);
			write(value);
		}
	};


	@Override
	public void setIOManager(Object serialInputOutputManager) {
		this.serialInputOutputManager = (SerialInputOutputManager) serialInputOutputManager;
	}

	@Override
	public void write(byte[] command) {
		try {
			// write the bytes to the arduino
			serialInputOutputManager.writeAsync(command);
		} catch (NullPointerException e) {}
	}
}
