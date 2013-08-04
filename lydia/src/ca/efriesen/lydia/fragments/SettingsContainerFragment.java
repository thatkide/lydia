package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia_common.includes.Constants;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;

/**
 * Created by eric on 2013-07-27.
 */
public class SettingsContainerFragment extends Fragment {

	private static final String TAG = "lydia settings container fragment";
	public SharedPreferences sharedPreferences;

	// new listener for our preferences change
	public SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
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
			}
		}
	};

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.settings_container_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		getFragmentManager().beginTransaction()
				.add(R.id.settings_controls, new SettingsControlsFragment())
				.add(R.id.settings_fragment, new SystemSettingsFragment())
				.commit();
	}
}
