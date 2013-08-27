package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.fragments.Settings.MediaSettingsFragment;
import ca.efriesen.lydia.fragments.Settings.NavigationSettingsFragment;
import ca.efriesen.lydia.fragments.Settings.SystemSettingsFragment;

/**
 * Created by eric on 2013-07-28.
 */
public class SettingsControlsFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.settings_controls_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		Activity activity = getActivity();

		// More button
		(activity.findViewById(R.id.settings_more)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.controls_slide_out_up, R.anim.controls_slide_in_up)
						.replace(R.id.settings_controls, new SettingsControlsMoreFragment())
						.commit();
			}
		});

		// system settings button
		(activity.findViewById(R.id.settings_system)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
						.replace(R.id.settings_fragment, new SystemSettingsFragment())
						.addToBackStack(null)
						.commit();
			}
		});

		// media button
		(activity.findViewById(R.id.settings_media)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
						.replace(R.id.settings_fragment, new MediaSettingsFragment())
						.addToBackStack(null)
						.commit();
			}
		});

		// nav button
		(activity.findViewById(R.id.settings_navigation)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
						.replace(R.id.settings_fragment, new NavigationSettingsFragment())
						.addToBackStack(null)
						.commit();
			}
		});
	}
}