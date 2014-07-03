package ca.efriesen.lydia.buttons;

import android.app.Activity;
import android.view.View;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.fragments.LauncherFragment;

/**
 * Created by eric on 2014-06-14.
 */
public class AndroidButton extends BaseButton {

	private Activity activity;

	public AndroidButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(View view, Button button) {
		activity.getFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.homescreen_slide_out_up, R.anim.homescreen_slide_in_up)
				.replace(R.id.home_screen_fragment, new LauncherFragment(), "launcherFragment")
				.addToBackStack(null)
				.commit();
	}

}
