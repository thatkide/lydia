package com.autosenseapp.fragments.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.autosenseapp.R;

/**
 * Created by eric on 2013-08-24.
 */
public class WeatherSettingsFragment extends PreferenceFragment {
	public static final String TAG = "lydia system Settings Preference";
	public SharedPreferences sharedPreferences;

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		addPreferencesFromResource(R.xml.weather_preferences_fragment);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		sharedPreferences = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
	}

}
