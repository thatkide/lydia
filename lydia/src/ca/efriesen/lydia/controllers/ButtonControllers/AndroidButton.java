package ca.efriesen.lydia.controllers.ButtonControllers;

import android.app.Activity;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.Dashboard;
import ca.efriesen.lydia.fragments.HomeScreenFragment;
import ca.efriesen.lydia.fragments.LauncherFragment;

/**
 * Created by eric on 2014-06-14.
 */
public class AndroidButton extends MyButton {

	public static final String ACTION = "AndroidButton";

	@Override
	public void onClick(Activity activity) {
		activity.getFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.homescreen_slide_out_up, R.anim.homescreen_slide_in_up)
				.replace(R.id.home_screen_fragment, new LauncherFragment(), "launcherFragment")
				.addToBackStack(null)
				.commit();
		((Dashboard)activity).setHomeScreenClass(HomeScreenFragment.class);
	}
}
