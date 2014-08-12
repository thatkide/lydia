package ca.efriesen.lydia.devices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import ca.efriesen.lydia.services.ArduinoService;
import ca.efriesen.lydia_common.includes.Constants;

/**
 * Created by eric on 2013-05-28.
 */
public class Defroster extends Device {
	private static final String TAG = "defroster";

	// application context.  we need this for the broadcast receiver
	private Context context;
	private ArduinoService.ArduinoListener listener;

	// constructor.  get the required info and pass it to the super class
	public Defroster(Context context, String intentFilter) {
		super(context);
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

	@Override
	public void setListener(ArduinoService.ArduinoListener listener) {
		this.listener = listener;
	}

	@Override
	public void parseData(int sender, int length, int[] data, int checksum) {

	}

	public void write(byte[] data) {

	}


	// this is broadcast from the ui controls
	private BroadcastReceiver defrosterReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// set the value to either 0 or 1, depending on if getbooleanextra is true or false
			byte data[] = {Constants.REARWINDOWDEFROSTER, 1, (intent.getBooleanExtra("state", false) ? (byte)1 : (byte)0)};

			// convert the window defroster constant into a byte array, and write it to the serial bus
			//listener.writeData(data);
		}
	};
}
