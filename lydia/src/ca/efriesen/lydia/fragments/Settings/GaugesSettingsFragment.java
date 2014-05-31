package ca.efriesen.lydia.fragments.Settings;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceFragment;
import android.util.Log;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.devices.IdiotLights;
import ca.efriesen.lydia.services.ArduinoService;

/**
 * Created by eric on 2014-05-19.
 */
public class GaugesSettingsFragment extends PreferenceFragment {
	public static final String TAG = "lydia gauges Settings Preference";

//	private ArduinoService arduinoService;
//	private IdiotLights idiotLights;
//	private boolean mBound = false;

	public SharedPreferences sharedPreferences;

	public SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
			if (s.equalsIgnoreCase("backlight")) {
				Log.d(TAG, "got backlight info");
//				byte data[] = {2, (byte)IdiotLights.BACKLIGHT, (byte)(sharedPreferences.getBoolean("backlight", true) ? 1 : 0)};
				Bundle data = new Bundle();
				data.putByte("command", (byte)IdiotLights.BACKLIGHT);
				data.putByte("value", (byte)(sharedPreferences.getBoolean("backlight", true) ? 1 : 0));
//					idiotLights.write(data);
				getActivity().sendBroadcast(new Intent(IdiotLights.IDIOTLIGHTSWRITE).putExtras(data));
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.gauge_preferences_fragment);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		// bind to the arduino service to get the objects needed to pass data back and forth
//		getActivity().bindService(new Intent(getActivity(), ArduinoService.class), mConnection, Context.BIND_AUTO_CREATE);

		sharedPreferences = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		sharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
	}

//	private ServiceConnection mConnection = new ServiceConnection() {
//		@Override
//		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//			ArduinoService.ArduinoBinder binder = (ArduinoService.ArduinoBinder) iBinder;
//			arduinoService = binder.getService();
//			idiotLights = (IdiotLights) arduinoService.getDevice(IdiotLights.id);
//			mBound = true;
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName componentName) {
//			mBound = false;
//		}
//	};
}
