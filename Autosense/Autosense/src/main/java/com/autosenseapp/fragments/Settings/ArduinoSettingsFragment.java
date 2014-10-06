package com.autosenseapp.fragments.Settings;

import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.os.Bundle;
import android.preference.*;
import com.autosenseapp.R;
import com.autosenseapp.devices.Master;
import com.autosenseapp.devices.usbInterfaces.ArduinoDevice;
import com.autosenseapp.services.ArduinoService;

import javax.inject.Inject;

/**
 * Created by eric on 2013-08-01.
 */
public class ArduinoSettingsFragment extends BasePreferenceFragment {
	public static final String TAG = ArduinoSettingsFragment.class.getSimpleName();

	@Inject	SharedPreferences sharedPreferences;

	public Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			String key = preference.getKey();
			if (key.equalsIgnoreCase("upgradeFirmware")) {
				getActivity().sendBroadcast(new Intent(ArduinoDevice.UPGRADE_FIRMWARE));

			} else if (key.equalsIgnoreCase("setupAlarm")) {
				// load the alarm settings fragment
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
						.replace(R.id.home_screen_fragment, new AlarmSettingsFragment())
						.addToBackStack(null)
						.commit();
			} else if (key.equalsIgnoreCase("setupGaugeCluster")) {
				// load the gauge cluster settings fragment
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
						.replace(R.id.home_screen_fragment, new GaugesSettingsFragment())
						.addToBackStack(null)
						.commit();
			} else if (key.equalsIgnoreCase("setupMasterIO")) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
						.replace(R.id.home_screen_fragment, new MasterIoFragment())
						.addToBackStack(null)
						.commit();
			}
			return false;
		}
	};

	public SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
//			if(s.equalsIgnoreCase("useLightSensor")) {
//				boolean useLightSensor = sharedPreferences.getBoolean("useLightSensor", false);
//			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.arduino_preferences_fragment);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		sharedPreferences.registerOnSharedPreferenceChangeListener(mListener);

		// set our internal preference for the bluetooth state
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		sharedPreferences.edit().putBoolean("systemBluetooth", adapter.isEnabled()).apply();

		findPreference("upgradeFirmware").setOnPreferenceClickListener(clickListener);
		findPreference("setupAlarm").setOnPreferenceClickListener(clickListener);
		findPreference("setupGaugeCluster").setOnPreferenceClickListener(clickListener);
		findPreference("setupMasterIO").setOnPreferenceClickListener(clickListener);

		if (sharedPreferences.getInt(ArduinoService.ARDUINO_TYPE, ArduinoService.ARDUINO_NONE) != ArduinoService.ARDUINO_DEVICE) {
			PreferenceCategory category = (PreferenceCategory) findPreference("arduino");
			category.removePreference(findPreference("autoUpgradeFirmware"));
			category.removePreference(findPreference("upgradeFirmware"));
		}

		getActivity().registerReceiver(lightValueReceiver, new IntentFilter(Master.LIGHTVALUE));
	}

	private BroadcastReceiver lightValueReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
//				findPreference("minLight").setSummary(getString(R.string.current_value) + ": " + intent.getStringExtra(Master.LIGHTVALUE));
//				findPreference("maxLight").setSummary(getString(R.string.current_value) + ": " + intent.getStringExtra(Master.LIGHTVALUE));
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
