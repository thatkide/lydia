package com.autosenseapp.fragments.Settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.autosenseapp.R;

/**
 * Created by eric on 2013-08-01.
 */
public class NavigationSettingsFragment extends PreferenceFragment {
	public static final String TAG = "lydia navigation Settings Preference";

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		addPreferencesFromResource(R.xml.navigation_preferences_fragment);
	}
}
