package ca.efriesen.lydia.controllers.ButtonControllers;

import android.app.Activity;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.Dashboard;
import ca.efriesen.lydia.fragments.HomeScreenFragment;
import ca.efriesen.lydia.fragments.MapContainerFragment;

/**
 * Created by eric on 2014-06-14.
 */
public class NavigationButton extends MyButton{

	public static final String ACTION = "NavigationButton";

	private Activity activity;

	public NavigationButton(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onClick() {
		activity.getFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
				.replace(R.id.home_screen_container_fragment, new MapContainerFragment(), "homeScreenContainerFragment")
				.addToBackStack(null)
				.commit();
		((Dashboard)activity).setHomeScreenClass(HomeScreenFragment.class);

	}

}
