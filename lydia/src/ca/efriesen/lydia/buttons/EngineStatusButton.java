package ca.efriesen.lydia.buttons;

import android.app.Activity;
import android.view.View;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.fragments.EngineStatusFragment;

/**
 * Created by eric on 2014-06-15.
 */
public class EngineStatusButton extends BaseButton {

	private Activity activity;

	public EngineStatusButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(View view, Button button) {
		activity.getFragmentManager().beginTransaction()
			.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
			.replace(R.id.home_screen_fragment, new EngineStatusFragment())
			.addToBackStack(null)
			.commit();
	}
}
