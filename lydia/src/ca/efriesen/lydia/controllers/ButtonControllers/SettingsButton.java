package ca.efriesen.lydia.controllers.ButtonControllers;

import android.app.Activity;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.fragments.SettingsContainerFragment;

/**
 * Created by eric on 2014-06-14.
 */
public class SettingsButton extends MyButton {

	public static final String ACTION = "SettingsButton";

	private Activity activity;

	public SettingsButton(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onClick() {
		// replace the 'dashboard_container' fragment with a new 'settings fragment'
		activity.getFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
				.replace(R.id.home_screen_container_fragment, new SettingsContainerFragment(), "homeScreenContainerFragment")
				.addToBackStack(null)
				.commit();
	}

	@Override
	public String getAction() {
		return ACTION;
	}

	@Override
	public String getDescription() {
		return "Open Program Settings";
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
