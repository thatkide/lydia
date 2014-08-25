package com.autosenseapp.buttons.appButtons;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import com.autosenseapp.R;
import com.autosenseapp.buttons.BaseButton;
import com.autosenseapp.callbacks.FragmentAnimationCallback;
import com.autosenseapp.databases.Button;
import com.autosenseapp.fragments.DriverControlsFragment;
import com.autosenseapp.fragments.MapFragment;
import com.autosenseapp.fragments.PassengerControlsFragment;

/**
 * Created by eric on 2014-06-14.
 */
public class NavigationButton extends BaseButton implements FragmentAnimationCallback {

	private Activity activity;

	public NavigationButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(View view, Button button) {
		FragmentManager manager = activity.getFragmentManager();
		PassengerControlsFragment fragment = (PassengerControlsFragment) manager.findFragmentById(R.id.passenger_controls);
		fragment.hideFragment(this);
		DriverControlsFragment driverControlsFragment = (DriverControlsFragment) manager.findFragmentById(R.id.driver_controls);
		driverControlsFragment.hideFragment(null);
	}

	@Override
	public void animationComplete(int direction) {
		FragmentManager manager = activity.getFragmentManager();
		if (direction == HIDE) {
			// start loading the settings fragment
			manager.beginTransaction()
					.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
					.replace(R.id.home_screen_fragment, new MapFragment())
					.addToBackStack(null)
					.commit();

			// Load new driver controls with nav buttons
			DriverControlsFragment driverControlsFragment = new DriverControlsFragment();
			Bundle args = new Bundle();
			args.putInt("group", BaseButton.GROUP_NAVIGATION);
			driverControlsFragment.setArguments(args);

			manager.beginTransaction()
					.setCustomAnimations(R.anim.controls_slide_out_up, R.anim.controls_slide_in_up)
					.replace(R.id.driver_controls, driverControlsFragment)
					.commit();
			// remove the controls view from the layout
			activity.findViewById(R.id.passenger_controls).setVisibility(View.GONE);
			activity.findViewById(R.id.driver_controls).setVisibility(GONE);
		}
	}

	@Override
	public void animationStart(int direction) { }
}
