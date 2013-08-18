package ca.efriesen.lydia.fragments.Settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import ca.efriesen.lydia.R;

/**
 * Created by eric on 2013-08-01.
 */
public class MediaSettingsFragment extends PreferenceFragment {
	public static final String TAG = "lydia media Settings Preference";

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		addPreferencesFromResource(R.xml.media_preferences_fragment);
	}
}
