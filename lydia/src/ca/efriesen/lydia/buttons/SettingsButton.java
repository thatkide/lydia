package ca.efriesen.lydia.buttons;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.callbacks.FragmentAnimationCallback;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.fragments.DriverControlsFragment;
import ca.efriesen.lydia.fragments.PassengerControlsFragment;
import ca.efriesen.lydia.fragments.Settings.SystemSettingsFragment;

/**
 * Created by eric on 2014-06-14.
 */
public class SettingsButton extends BaseButton implements FragmentAnimationCallback{

	private Activity activity;

	public SettingsButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	// This being our entry point into the settings pane, we need a little bit more checking to properly animate fragments correctly
	@Override
	public void onClick(View view, Button button) {

		// FIXME
		// check if instance of settings fragment and passneger controls visible
		// yes, hide it, load settings
		// check if passenger contols not visible
		// check if instance of settings fragment
		// no, load it

		FragmentManager manager = activity.getFragmentManager();
		Fragment currentFragment = manager.findFragmentById(R.id.home_screen_fragment);
		// settings isn't in place.  animate the passenger controls out, and show the settings
		if (activity.findViewById(R.id.passenger_controls).getVisibility() == View.GONE && !(currentFragment instanceof SystemSettingsFragment) ) {
			manager.beginTransaction()
					.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
					.replace(R.id.home_screen_fragment, new SystemSettingsFragment())
					.addToBackStack(null)
					.commit();
		} else {
			if (!(currentFragment instanceof SystemSettingsFragment)) {
				PassengerControlsFragment fragment = (PassengerControlsFragment) manager.findFragmentById(R.id.passenger_controls);
				fragment.hideFragment(this);
			}
		}
	}

	@Override
	public void animationComplete(int direction) {
		FragmentManager manager = activity.getFragmentManager();
		if (direction == HIDE) {
			// start loading the settings fragment
			manager.beginTransaction()
					.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
					.replace(R.id.home_screen_fragment, new SystemSettingsFragment())
					.addToBackStack(null)
					.commit();

			DriverControlsFragment driverControlsFragment = new DriverControlsFragment();
			Bundle args = new Bundle();
			args.putInt("selectedDriverBar", 0);
			args.putInt("group", BaseButton.GROUP_ADMIN);
			driverControlsFragment.setArguments(args);

			manager.beginTransaction()
					.setCustomAnimations(R.anim.controls_slide_out_up, R.anim.controls_slide_in_up)
					.replace(R.id.driver_controls, driverControlsFragment)
					.addToBackStack(null)
					.commit();
			// remove the controls view from the layout
			activity.findViewById(R.id.passenger_controls).setVisibility(View.GONE);
		}
	}
}
