package ca.efriesen.lydia.fragments.Settings;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.devices.IdiotLights;
import ca.efriesen.lydia.includes.Helpers;

/**
 * Created by eric on 2014-05-19.
 */
public class GaugesSettingsFragment extends PreferenceFragment {
	public static final String TAG = "lydia gauges Settings Preference";

	private Activity activity;
	public SharedPreferences sharedPreferences;

	public SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
			// create a new bundle
			Bundle data = new Bundle();
			// if the backlight pref has changed
			if (s.equalsIgnoreCase("backlightBrightness")) {
				// convert the float of the slider (0.0 - 1.0) to a range of 0-255
				byte value[] = {(byte)Math.round(sharedPreferences.getFloat("backlightBrightness", 0) * 255)};
				// put the command and value we want to send out into the bundle
				data.putByte("command", (byte) IdiotLights.BACKLIGHT);
				data.putByteArray("values", value);
			} else if (s.equalsIgnoreCase("backlightAutoBrightness")) {
				// convert the float of the slider (0.0 - 1.0) to a range of 0-255
				byte value[] = {(sharedPreferences.getBoolean("backlightAutoBrightness", false) ? (byte)1 : (byte)0)};
				// put the command and value we want to send out into the bundle
				data.putByte("command", (byte) IdiotLights.BACKLIGHTAUTOBRIGHTNESS);
				data.putByteArray("values", value);
			} else if(s.equalsIgnoreCase("speedoInputPulses")) {
				int pulses = Integer.parseInt(sharedPreferences.getString("speedoInputPulses", "0"));
				byte values[] = {Helpers.highByte(pulses), Helpers.lowByte(pulses)};
				data.putByte("command", (byte)IdiotLights.SPEEDOINPULSES);
				data.putByteArray("values", values);
			} else if(s.equalsIgnoreCase("speedoOutputPulses")) {
				int pulses = Integer.parseInt(sharedPreferences.getString("speedoOutputPulses", "0"));
				byte values[] = {Helpers.highByte(pulses), Helpers.lowByte(pulses)};
				data.putByte("command", (byte)IdiotLights.SPEEDOOUTPULSES);
				data.putByteArray("values", values);
			}
			// send a broadcast with the data
			activity.sendBroadcast(new Intent(IdiotLights.WRITE).putExtras(data));
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
		// save the activity to a member variable, we use it a bunch
		activity = getActivity();

		// get the shared preferences for our app
		sharedPreferences = activity.getSharedPreferences(activity.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		// set a listener for preferences being changed
		sharedPreferences.registerOnSharedPreferenceChangeListener(mListener);

	}
}
