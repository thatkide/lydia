package ca.efriesen.lydia.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import ca.efriesen.lydia.R;

/**
 * Created by eric on 2013-08-01.
 */
public class NavigationSettingsFragment extends PreferenceFragment {
	public static final String TAG = "lydia navigation Settings Preference";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.navigation_preferences_fragment);
	}
}
