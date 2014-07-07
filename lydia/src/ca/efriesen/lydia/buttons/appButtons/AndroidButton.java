package ca.efriesen.lydia.buttons.appButtons;

import android.app.Activity;
import android.view.View;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia.buttons.BaseButton;
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
				.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
				.replace(R.id.home_screen_fragment, new LauncherFragment())
				.addToBackStack(null)
				.commit();
	}

}
