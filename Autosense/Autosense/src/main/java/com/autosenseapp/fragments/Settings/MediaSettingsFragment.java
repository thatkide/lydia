package com.autosenseapp.fragments.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;
import com.autosenseapp.R;
import com.autosenseapp.includes.CustomSwitchPreference;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;

/**
 * Created by eric on 2013-08-01.
 */
public class MediaSettingsFragment extends PreferenceFragment {
	public static final String TAG = "lydia media Settings Preference";
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
										getActivity().getString(R.string.lastFmKey),
										getActivity().getString(R.string.lastFmSecret)
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
			// enable the two to be mutually exclusive
			} else if (s.equalsIgnoreCase("useAlbumArtBg")) {
				if (sharedPreferences.getBoolean("useAlbumArtBg", false)) {
					((CustomSwitchPreference) findPreference("useArtistArtBg")).setChecked(false);
				}
			} else if (s.equalsIgnoreCase("useArtistArtBg")) {
				if (sharedPreferences.getBoolean("useArtistArtBg", false)) {
					((CustomSwitchPreference) findPreference("useAlbumArtBg")).setChecked(false);
				}
			}
		}
	};

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		sharedPreferences = getActivity().getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		sharedPreferences.registerOnSharedPreferenceChangeListener(mListener);

		addPreferencesFromResource(R.xml.media_preferences_fragment);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener);
	}

}
