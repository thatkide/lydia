package ca.efriesen.lydia.controllers.ButtonControllers;

import android.app.Activity;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.fragments.MusicFragment;

/**
 * Created by eric on 2014-06-14.
 */
public class MusicButton extends MyButton {

	public static final String ACTION = "MusicButton";

	@Override
	public void onClick(Activity activity) {
		activity.getFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.homescreen_slide_out_up, R.anim.homescreen_slide_in_up)
				.replace(R.id.home_screen_fragment, new MusicFragment(), "musicFragment")
				.addToBackStack(null)
				.commit();
	}
}
