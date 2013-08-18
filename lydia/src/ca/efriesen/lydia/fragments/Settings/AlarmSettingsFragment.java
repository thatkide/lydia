package ca.efriesen.lydia.fragments.Settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import ca.efriesen.lydia.R;

/**
 * Created by eric on 2013-08-17.
 */
public class AlarmSettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.alarm_preferences_fragment);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
	}

}
