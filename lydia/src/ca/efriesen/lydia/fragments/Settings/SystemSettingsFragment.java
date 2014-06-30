package ca.efriesen.lydia.fragments.Settings;

import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia_common.includes.Intents;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
			if(s.equalsIgnoreCase("systemBluetooth")) {
				boolean systemBluetooth = sharedPreferences.getBoolean("systemBluetooth", false);
				// apply the changes now
				sharedPreferences.edit().putBoolean("systemBluetooth", systemBluetooth).apply();
				BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
				if (systemBluetooth) {
					adapter.enable();
				} else {
					adapter.disable();
				}
			} else if(s.equalsIgnoreCase("useBluetooth")) {
				Log.d(TAG, "use bt changed");
				boolean useBluetooth = sharedPreferences.getBoolean("useBluetooth", false);
				// apply the changes now
				getActivity().sendBroadcast(new Intent(Intents.BLUETOOTHMANAGER).putExtra("useBluetooth", useBluetooth));
			} else if(s.equalsIgnoreCase("systemWiFi")) {
				WifiManager manager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
				// change state of wifi
				manager.setWifiEnabled(sharedPreferences.getBoolean("systemWiFi", false));
			}
		}
	};

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		addPreferencesFromResource(R.xml.system_preferences_fragment);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		sharedPreferences = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		sharedPreferences.registerOnSharedPreferenceChangeListener(mListener);

		// get our radio managers
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		WifiManager manager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

		Long lastUpdateCheck = sharedPreferences.getLong("lastUpdateCheck", 0);
		Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
		calendar.setTimeInMillis(lastUpdateCheck);
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");


		Preference updateCheck = findPreference("checkForUpdate");
		updateCheck.setSummary("Last checked: " + format.format(calendar.getTime()));

		// set our internal preference for the bluetooth state
		sharedPreferences.edit()
				.putBoolean("systemBluetooth", adapter.isEnabled())
		// set the wifi state
				.putBoolean("systemWiFi", manager.isWifiEnabled())
				.apply();
	}
}