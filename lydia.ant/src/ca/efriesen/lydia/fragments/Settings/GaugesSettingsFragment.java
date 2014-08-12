package ca.efriesen.lydia.fragments.Settings;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.devices.IdiotLights;
import ca.efriesen.lydia.includes.Helpers;
import net.jayschwa.android.preference.SliderPreferenceCallback;
import net.jayschwa.android.preference.SliderPreference;

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
			// if the backlight pref has changed
			if (s.equalsIgnoreCase("backlightBrightness")) {
				// convert the float of the slider (0.0 - 1.0) to a range of 0-255
				byte values[] = {(byte)Math.round(sharedPreferences.getFloat("backlightBrightness", 0) * 255)};
				IdiotLights.writeData(activity, IdiotLights.BACKLIGHT, values);
			// Auto backlight
			} else if (s.equalsIgnoreCase("backlightAutoBrightness")) {
				byte value[] = {(sharedPreferences.getBoolean("backlightAutoBrightness", false) ? (byte) 1 : (byte) 0)};
				IdiotLights.writeData(activity, IdiotLights.BACKLIGHTAUTOBRIGHTNESS, value);
			// Speaker
			} else if(s.equalsIgnoreCase("speaker")) {
				byte value[] = {(sharedPreferences.getBoolean("speaker", false) ? (byte) 1 : (byte) 0)};
				IdiotLights.writeData(activity, IdiotLights.SPEAKER, value);
			// Speaker Volume test
			} else if (s.equalsIgnoreCase("speakerVolume")) {
				// convert the float of the slider (0.0 - 1.0) to a range of 0-10
				byte value[] = {(byte) Math.round(sharedPreferences.getFloat("speakerVolume", 0) * 10)};
				IdiotLights.writeData(activity, IdiotLights.SPEAKERVOLUMETEST, value);
			// Speedo input pulses
			} else if(s.equalsIgnoreCase("speedoInputPulses")) {
				int pulses = Integer.parseInt(sharedPreferences.getString("speedoInputPulses", "0"));
				byte values[] = {Helpers.highByte(pulses), Helpers.lowByte(pulses)};
				IdiotLights.writeData(activity, IdiotLights.SPEEDOINPULSES, values);
			// Speedo output pulses
			} else if(s.equalsIgnoreCase("speedoOutputPulses")) {
				int pulses = Integer.parseInt(sharedPreferences.getString("speedoOutputPulses", "0"));
				byte values[] = {Helpers.highByte(pulses), Helpers.lowByte(pulses)};
				IdiotLights.writeData(activity, IdiotLights.SPEEDOOUTPULSES, values);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.gauge_preferences_fragment);
		// get the backlight slider and set it to auto update.  this allows us to change the value without having to press ok every time to see it
		SliderPreference backlightBrightness = (SliderPreference) findPreference("backlightBrightness");
		backlightBrightness.setAutoUpdate(true);

		SliderPreference speakerVolume = (SliderPreference) findPreference("speakerVolume");
		speakerVolume.setAutoUpdate(true);
		// set a onDialogClosed for our volume slider
		speakerVolume.sliderPreferenceCallback = new SliderPreferenceCallback() {
			@Override
			public void onDialogClosed(boolean positiveResult) {
				if (positiveResult) {
					// convert the float of the slider (0.0 - 1.0) to a range of 0-10
					byte value[] = {(byte) Math.round(sharedPreferences.getFloat("speakerVolume", 0) * 10)};
					IdiotLights.writeData(activity, IdiotLights.SPEAKERVOLUMESAVE, value);
				}
			}
		};
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
