package ca.efriesen.lydia.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ca.efriesen.lydia.R;

/**
 * Created by eric on 2013-07-27.
 */
public class SettingsContainerFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.settings_container_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		getFragmentManager().beginTransaction()
				.add(R.id.settings_controls, new SettingsControlsFragment())
				.add(R.id.settigns_fragment, new SettingsFragment())
				.commit();
	}
}
