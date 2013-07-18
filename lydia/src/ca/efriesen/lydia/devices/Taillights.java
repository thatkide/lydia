package ca.efriesen.lydia.devices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import ca.efriesen.lydia.interfaces.SerialIO;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.nio.ByteBuffer;

/**
 * Created by eric on 2013-05-28.
 */
public class Taillights extends Device implements SerialIO {
	private static final String TAG = "taillights";

	// application context.  we need this for the broadcast receiver
	private Context context;
	private SerialInputOutputManager serialInputOutputManager = null;

	// constructor.  get the required info and pass it to the super class
	public Taillights(Context context, int id, String intentFilter) {
		super(context, id, intentFilter);
		this.context = context;

		// register the broadcast receiver
		context.registerReceiver(taillightsReceiver, new IntentFilter(intentFilter));
	}

	// here we do cleanup tasks, such as unregistering the broadcast receiver
	@Override
	public void cleanUp() {
		try {
			context.unregisterReceiver(taillightsReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// this is broadcast from the ui controls
	private BroadcastReceiver taillightsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// convert to a byte array, and write it to the serial bus
			write(ByteBuffer.allocate(4).putInt(intent.getIntExtra("light", 0)).array());
			write(ByteBuffer.allocate(4).putInt(intent.getIntExtra("brightness", 0)).array());
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
