package ca.efriesen.lydia.fragments;

import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.os.Bundle;
import android.preference.*;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia_common.includes.Intents;

/**
 * Created by eric on 2013-08-01.
 */
public class ArduinoSettingsFragment extends PreferenceFragment {
	public static final String TAG = "lydia sensor Settings Preference";

	public SharedPreferences sharedPreferences;

	public Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			if (preference.getKey().equalsIgnoreCase("upgradeFirmware")) {
				getActivity().sendBroadcast(new Intent("upgradeFirmware"));
			}
			return false;
		}
	};

	public SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
			if(s.equalsIgnoreCase("useLightSensor")) {
				boolean useLightSensor = sharedPreferences.getBoolean("useLightSensor", false);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPreferences.registerOnSharedPreferenceChangeListener(mListener);

		// set our internal preference for the bluetooth state
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		sharedPreferences.edit().putBoolean("systemBluetooth", adapter.isEnabled()).commit();

		addPreferencesFromResource(R.xml.arduino_preferences_fragment);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		Preference upgradeFirmware = getPreferenceManager().findPreference("upgradeFirmware");
		upgradeFirmware.setOnPreferenceClickListener(clickListener);

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
		} catch (Exception e) {}
	}

}
