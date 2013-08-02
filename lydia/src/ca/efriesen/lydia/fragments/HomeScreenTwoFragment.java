package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.ContactList;
import ca.efriesen.lydia.activities.Dashboard;

/**
 * User: eric
 * Date: 2013-03-13
 * Time: 10:00 PM
 */
public class HomeScreenTwoFragment extends Fragment{

	Activity activity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.home_screen_two_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = getActivity();

		final Button contacts = (Button) activity.findViewById(R.id.contacts);
		final Button homeScreenNext = (Button) activity.findViewById(R.id.home_screen_next);
		final Button homeScreenPrev = (Button) activity.findViewById(R.id.home_screen_previous);
		final Button settings = (Button) activity.findViewById(R.id.settings);

		contacts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				activity.startActivity(new Intent(activity, ContactList.class));
			}
		});

		homeScreenNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.controls_slide_in_left, R.anim.controls_slide_in_right)
						.replace(R.id.home_screen_fragment, new HomeScreenFragment(), "homeScreenFragment")
						.addToBackStack(null)
						.commit();
				((Dashboard)getActivity()).setHomeScreenClass(HomeScreenFragment.class);
			}
		});

		homeScreenPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.controls_slide_out_left, R.anim.controls_slide_out_right)
						.replace(R.id.home_screen_fragment, new HomeScreenFragment(), "homeScreenFragment")
						.addToBackStack(null)
						.commit();
				((Dashboard)getActivity()).setHomeScreenClass(HomeScreenFragment.class);
			}
		});

		settings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// replace the 'dashboard_container' fragment with a new 'settings fragment'
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
						.replace(R.id.home_screen_container_fragment, new SettingsContainerFragment(), "homeScreenContainerFragment")
						.addToBackStack("settingsFragment")
						.commit();
			}
		});
	}
}