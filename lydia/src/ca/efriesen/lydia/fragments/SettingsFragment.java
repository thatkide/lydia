package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia_common.includes.Constants;
import ca.efriesen.lydia_common.includes.Intents;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;

/**
 * User: eric
 * Date: 2012-10-24
 * Time: 1:09 PM
 */
public class SettingsFragment extends PreferenceFragment {
	public static final String TAG = "lydia Settings Preference";

	public SharedPreferences sharedPreferences;
	// new listener for our preferences change
	public SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
			Log.d(TAG, "pref change " + s);

			// if the lastfm password has changed
			if (s.equalsIgnoreCase("lastFmPassword") || s.equalsIgnoreCase("useLastFm")) {
				// try a lastfm login, we we want to use it
				if (sharedPreferences.getBoolean("useLastFm", false)) {
					final String lastFmUser = sharedPreferences.getString("lastFmUsername", null);
					final String lastFmPass = sharedPreferences.getString("lastFmPassword", null);

					// new thread.  networking can't be on the main thread
					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							// will be written to the final "toastMessage" var.  this way it can be accessed inside the runnable
							String message;
							try {
								// set cache to null
								Caller.getInstance().setCache(null);
								// try to acquire a session
								Authenticator.getMobileSession(
										lastFmUser,
										lastFmPass,
										Constants.lastFmKey,
										Constants.lastFmSecret
								);
								// set our status message
								message = getString(R.string.lastfm_login_successful);
							} catch (Exception e) {
								// we failed, set a failed message
								message = getString(R.string.lastfm_login_failed);
								Log.d(TAG, e.toString());
							}
							// set the toast message to the status message we had
							final String toastMessage = message;
							final Activity activity = getActivity();
							try {
								// run a toast on the ui thread
								activity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(activity.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
									}
								});
							} catch (Exception e) {}

							// Die
							Thread.currentThread().interrupt();
							return;
						}
					});
					thread.start();
				}
			} else if(s.equalsIgnoreCase("useBluetooth")) {
				boolean useBluetooth = sharedPreferences.getBoolean("useBluetooth", false);
				getActivity().sendBroadcast(new Intent(Intents.BLUETOOTHMANAGER).putExtra("useBluetooth", useBluetooth));
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		sharedPreferences.registerOnSharedPreferenceChangeListener(mListener);

		getActivity().registerReceiver(lightValueReceiver, new IntentFilter(Intents.LIGHTVALUE));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener);
		try {
			getActivity().unregisterReceiver(lightValueReceiver);
		} catch (IllegalArgumentException e) {}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		FragmentManager manager = getFragmentManager();

		// make sure settings is hidden
		manager.beginTransaction().hide(manager.findFragmentById(R.id.settings_fragment)).commit();

		Button settings = (Button) getActivity().findViewById(R.id.settings);
		settings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// replace the 'dashboard_container' fragment with a new 'settings fragment'
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction()
						.hide(fragmentManager.findFragmentById(R.id.home_screen_container_fragment))
						.hide(fragmentManager.findFragmentById(R.id.home_screen_fragment_two))
						.hide(fragmentManager.findFragmentById(R.id.map_container_fragment))
						.show(fragmentManager.findFragmentById(R.id.settings_fragment))
						.addToBackStack(null)
						.commit();
			}
		});
	}

	public boolean onBackPressed() {
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.show(fragmentManager.findFragmentById(R.id.home_screen_container_fragment))
				.show(fragmentManager.findFragmentById(R.id.home_screen_fragment_two))
				.hide(fragmentManager.findFragmentById(R.id.map_container_fragment))
				.hide(fragmentManager.findFragmentById(R.id.settings_fragment))
				.addToBackStack(null)
				.commit();
		return true;
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
}