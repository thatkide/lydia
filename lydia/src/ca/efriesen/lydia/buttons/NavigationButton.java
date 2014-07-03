package ca.efriesen.lydia.buttons;

import android.app.Activity;
import android.view.View;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.fragments.MapContainerFragment;

/**
 * Created by eric on 2014-06-14.
 */
public class NavigationButton extends BaseButton {

	private Activity activity;

	public NavigationButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(View view, Button button) {
		activity.getFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
				.replace(R.id.home_screen_container_fragment, new MapContainerFragment(), "homeScreenContainerFragment")
				.addToBackStack(null)
				.commit();
	}

}
