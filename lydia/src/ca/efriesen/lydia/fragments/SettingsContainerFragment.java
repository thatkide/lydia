package ca.efriesen.lydia.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.fragments.Settings.SystemSettingsFragment;

/**
 * Created by eric on 2013-07-27.
 */
public class SettingsContainerFragment extends Fragment {

	private static final String TAG = "lydia settings container fragment";

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		getFragmentManager().beginTransaction()
				.add(R.id.settings_controls, new SettingsControlsFragment())
				.add(R.id.settings_fragment, new SystemSettingsFragment()) // if this is changed, onbackpressed in dashboard.java needs to be changed too.  TODO. make this variable
				.commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.settings_container_fragment, container, false);
	}

}
