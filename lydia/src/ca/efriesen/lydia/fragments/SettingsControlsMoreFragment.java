package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.fragments.Settings.ArduinoSettingsFragment;
import ca.efriesen.lydia.fragments.Settings.WeatherSettingsFragment;

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
		(activity.findViewById(R.id.settings_arduino)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
						.replace(R.id.settings_fragment, new ArduinoSettingsFragment())
						.addToBackStack(null)
						.commit();
			}
		});

		// weather settings
		(activity.findViewById(R.id.settings_weather)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
						.replace(R.id.settings_fragment, new WeatherSettingsFragment())
						.addToBackStack(null)
						.commit();
			}
		});
	}
}
