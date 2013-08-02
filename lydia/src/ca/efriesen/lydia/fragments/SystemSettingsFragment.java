package ca.efriesen.lydia.fragments;

import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia_common.includes.Constants;
import ca.efriesen.lydia_common.includes.Intents;

/**
 * User: eric
 * Date: 2012-10-24
 * Time: 1:09 PM
 */
public class SystemSettingsFragment extends PreferenceFragment {

	public static final String TAG = "lydia system Settings Preference";
	public SharedPreferences sharedPreferences;

	public SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
			Log.d(TAG, "pref change " + s);

			if(s.equalsIgnoreCase("systemBluetooth")) {
				boolean systemBluetooth = sharedPreferences.getBoolean("systemBluetooth", false);
				BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
				if (systemBluetooth) {
					adapter.enable();
				} else {
					adapter.disable();
				}
			} else if(s.equalsIgnoreCase("useBluetooth")) {
				boolean useBluetooth = sharedPreferences.getBoolean("useBluetooth", false);
				getActivity().sendBroadcast(new Intent(Intents.BLUETOOTHMANAGER).putExtra("useBluetooth", useBluetooth));
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		sharedPreferences.registerOnSharedPreferenceChangeListener(mListener);

		addPreferencesFromResource(R.xml.system_preferences_fragment);

		// set our internal preference for the bluetooth state
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		sharedPreferences.edit().putBoolean("systemBluetooth", adapter.isEnabled()).commit();

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

}