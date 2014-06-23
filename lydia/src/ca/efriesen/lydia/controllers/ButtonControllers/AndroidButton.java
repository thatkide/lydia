package ca.efriesen.lydia.controllers.ButtonControllers;

import android.app.Activity;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.fragments.LauncherFragment;

/**
 * Created by eric on 2014-06-14.
 */
public class AndroidButton extends BaseButton {

	public static final String ACTION = "AndroidButton";

	private Activity activity;

	public AndroidButton(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onClick(Button button) {
		activity.getFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.homescreen_slide_out_up, R.anim.homescreen_slide_in_up)
				.replace(R.id.home_screen_fragment, new LauncherFragment(), "launcherFragment")
				.addToBackStack(null)
				.commit();
	}

	@Override
	public String getAction() {
		return ACTION;
	}

	@Override
	public String getDescription() {
		return "Open Application launcher";
	}

	@Override
	public String toString() {
		return getDescription();
	}

}
