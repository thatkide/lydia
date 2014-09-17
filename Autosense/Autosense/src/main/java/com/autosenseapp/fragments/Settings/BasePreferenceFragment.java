package com.autosenseapp.fragments.Settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.autosenseapp.AutosenseApplication;
import javax.inject.Inject;

/**
 * Created by eric on 2014-09-16.
 */
public abstract class BasePreferenceFragment extends PreferenceFragment {

	@Inject
	SharedPreferences sharedPreferences;

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		((AutosenseApplication)getActivity().getApplication()).inject(this);
	}
}
