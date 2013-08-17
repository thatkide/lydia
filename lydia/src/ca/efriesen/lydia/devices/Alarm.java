package ca.efriesen.lydia.devices;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import ca.efriesen.lydia.interfaces.SerialIO;
import ca.efriesen.lydia_common.includes.Constants;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.util.ArrayList;

/**
 * Created by eric on 2013-08-15.
 */
public class Alarm extends Device implements SerialIO {
	private static final String TAG = "lydia alarm";
	private static final int ID = 14;

	private Context context;
	private SerialInputOutputManager serialInputOutputManager = null;

	public Alarm(Context context, int id, String intentFilter) {
		super(context, id, intentFilter);
		this.context = context;
	}

	@Override
	public void initialize() {
		// check the preferences if we have auto find slaves turned on
		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("autoFindSlaveDevices", true)) {
			// we do, so send a ping request to the alarm module
			byte[] pingRequest = {Constants.PINGREQUEST, ID};
			write(pingRequest);
			// set the alarm to off, if we get a reply, we'll turn it back on
			PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("useAlarmModule", false).apply();
		}
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void setIOManager(Object serialInputOutputManager) {
		this.serialInputOutputManager = (SerialInputOutputManager) serialInputOutputManager;
	}

	@Override
	public void write(byte[] command) {
		// write the bytes to the arduino
		serialInputOutputManager.writeAsync(command);
	}

	@Override
	public void setValue(ArrayList<String> commands) {
		// get the value sent from the alarm
		int value = Integer.parseInt(commands.get(1));
		switch (value) {
			case Constants.PINGREPLY: {
				PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("useAlarmModule", true).apply();
			}
		}
	}
}
