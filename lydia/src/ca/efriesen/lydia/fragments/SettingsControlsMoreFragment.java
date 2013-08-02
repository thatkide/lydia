package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ca.efriesen.lydia.R;

/**
 * Created by eric on 2013-08-01.
 */
public class SettingsControlsMoreFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.settings_controls_more_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		Activity activity = getActivity();

		// back button
		(activity.findViewById(R.id.settings_back)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.controls_slide_in_down, R.anim.controls_slide_out_down)
						.replace(R.id.settings_controls, new SettingsControlsFragment())
						.commit();
			}
		});

		// sensors button
		(activity.findViewById(R.id.settings_sensors)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
						.replace(R.id.settings_fragment, new SensorsSettingsFragment())
						.addToBackStack(null)
						.commit();
			}
		});

	}
}
