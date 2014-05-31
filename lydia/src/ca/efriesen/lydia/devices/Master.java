package ca.efriesen.lydia.devices;

import android.content.Context;
import ca.efriesen.lydia.services.ArduinoService;

/**
 * Created by eric on 2014-05-04.
 */
public class Master extends Device {

	public static final int id = 10;
	private Context context;
	private ArduinoService.ArduinoListener listener;

	public Master(Context context) {
		this.context = context;
	}

	@Override
	public void cleanUp() {

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
		listener.writeData(data, id);
	}

}
