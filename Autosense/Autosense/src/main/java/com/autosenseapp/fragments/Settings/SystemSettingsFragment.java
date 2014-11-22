package com.autosenseapp.fragments.Settings;

import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import com.autosenseapp.R;
import com.autosenseapp.callbacks.FragmentAnimationCallback;
import com.autosenseapp.callbacks.FragmentOnBackPressedCallback;
import com.autosenseapp.fragments.DriverControlsFragment;
import com.autosenseapp.fragments.HomeScreenFragment;
import com.autosenseapp.fragments.NotificationFragments.SystemNotificationFragment;
import com.autosenseapp.fragments.PassengerControlsFragment;
import ca.efriesen.lydia_common.includes.Intents;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * User: eric
 * Date: 2012-10-24
 * Time: 1:09 PM
 */
public class SystemSettingsFragment extends PreferenceFragment implements FragmentAnimationCallback, FragmentOnBackPressedCallback, Preference.OnPreferenceClickListener {

	private static final String TAG = SystemNotificationFragment.class.getSimpleName();

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

		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		sharedPreferences.registerOnSharedPreferenceChangeListener(mListener);

		// get our radio managers
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		WifiManager manager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

		Long lastUpdateCheck = sharedPreferences.getLong("lastUpdateCheck", 0);
		Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
		calendar.setTimeInMillis(lastUpdateCheck);
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");

		Preference updateCheck = findPreference("checkForUpdate");
		updateCheck.setSummary("Last checked: " + (lastUpdateCheck != 0 ? format.format(calendar.getTime()) : getActivity().getString(R.string.never)));

		// set our internal preference for the bluetooth state
		sharedPreferences.edit()
				.putBoolean("systemBluetooth", adapter.isEnabled())
		// set the wifi state
				.putBoolean("systemWiFi", manager.isWifiEnabled())
				.apply();

		Preference background = findPreference("background");
		background.setOnPreferenceClickListener(this);

		try {
			PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			Preference versionNumber = findPreference("versionNumber");
			versionNumber.setTitle(R.string.build_number);
			versionNumber.setSummary(String.valueOf(info.versionCode));
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			Log.d(TAG, e.toString());
		}
	}

	@Override
	public void onBackPressed() {
		((PassengerControlsFragment) getActivity().getFragmentManager().findFragmentById(R.id.passenger_controls)).showFragment(this);
	}

	@Override
	public void animationComplete(int direction) {
		FragmentManager manager = getFragmentManager();
		manager.beginTransaction()
				.setCustomAnimations(R.anim.container_slide_in_down, R.anim.container_slide_out_down)
				.replace(R.id.home_screen_fragment, new HomeScreenFragment())
				.addToBackStack(null)
				.commit();

		manager.beginTransaction()
				.setCustomAnimations(R.anim.container_slide_in_down, R.anim.container_slide_out_down)
				.replace(R.id.driver_controls, new DriverControlsFragment())
				.commit();
	}

	@Override
	public void animationStart(int direction) { }

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equalsIgnoreCase("background")) {
			getFragmentManager().beginTransaction()
					.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
				.replace(R.id.home_screen_fragment, new BackgroundSettingsFragment())
					.addToBackStack(null)
				.commit();
		}
		return true;
	}
}