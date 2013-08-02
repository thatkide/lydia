package ca.efriesen.lydia.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia_common.includes.Intents;

/**
 * Created by eric on 2013-08-01.
 */
public class SensorsSettingsFragment extends PreferenceFragment {
	public static final String TAG = "lydia sensor Settings Preference";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.sensor_preferences_fragment);
		getActivity().registerReceiver(lightValueReceiver, new IntentFilter(Intents.LIGHTVALUE));
	}

	private BroadcastReceiver lightValueReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				findPreference("minLight").setSummary(getString(R.string.current_value) + ": " + intent.getStringExtra(Intents.LIGHTVALUE));
				findPreference("maxLight").setSummary(getString(R.string.current_value) + ": " + intent.getStringExtra(Intents.LIGHTVALUE));
			} catch (NullPointerException e) { e.printStackTrace();}
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			getActivity().unregisterReceiver(lightValueReceiver);
		} catch (IllegalArgumentException e) {}
	}

}
