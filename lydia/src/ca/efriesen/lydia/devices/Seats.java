package ca.efriesen.lydia.devices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import ca.efriesen.lydia.services.ArduinoService;
import ca.efriesen.lydia_common.includes.Intents;

/**
 * Created by eric on 2013-05-28.
 */
public class Seats extends Device {
	private static final String TAG = "seats";

	private Context context;
	private byte id;
	private ArduinoService.ArduinoListener listener;

	public Seats(Context context, byte id) {
		this.context = context;
		this.id = id;

		context.registerReceiver(seatsReceiver, new IntentFilter(Intents.SEATHEAT));
	}


	@Override
	public void cleanUp() {
		try {
			context.unregisterReceiver(seatsReceiver);
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

	@Override
	public void write(byte[] data) {

	}


	private BroadcastReceiver seatsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			byte seatId = intent.getByteExtra("seatId", (byte)0);
			int temperature = intent.getIntExtra("temp", 0);

			// is this driver or passenger seat0
			if (seatId == id) {
				// create the new data array.  the seat id, the length of the data, and the data
				byte data[] = {id, 1, (byte)temperature};
			//	listener.writeData(data);
			}
		}
	};
}
