package ca.efriesen.lydia.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ca.efriesen.lydia.R;

/**
 * Created by eric on 2013-07-28.
 */
public class SettingsControlsFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.settings_controls_fragment, container, false);
	}
}