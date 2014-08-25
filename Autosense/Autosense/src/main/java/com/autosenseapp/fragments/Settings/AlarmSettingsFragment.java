package com.autosenseapp.fragments.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.autosenseapp.R;
import ca.efriesen.lydia_common.includes.Intents;

/**
 * Created by eric on 2013-08-17.
 */
public class AlarmSettingsFragment extends PreferenceFragment {
	public static final String TAG = "lydia alarm Settings Preference";

	public SharedPreferences sharedPreferences;

	public SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
			if(s.equalsIgnoreCase("autoArm")) {
				// set auto arm
				Intent update = new Intent(Intents.ALARM);
				update.putExtra("autoArm", sharedPreferences.getBoolean("autoArm", true));
				getActivity().sendBroadcast(update);
			} else if (s.equalsIgnoreCase("autoArmDelay")) {
				// update auto arm delay
				Intent update = new Intent(Intents.ALARM);
				update.putExtra("autoArmDelay", Integer.parseInt(sharedPreferences.getString("autoArmDelay", "30")));
				getActivity().sendBroadcast(update);
			} else if (s.equalsIgnoreCase("alarmLength")) {
				// set the alarm length
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.alarm_preferences_fragment);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		sharedPreferences = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		sharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
	}

}
