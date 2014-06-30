package ca.efriesen.lydia.controllers.ButtonControllers;

import android.app.Activity;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.fragments.EngineStatusFragment;

/**
 * Created by eric on 2014-06-15.
 */
public class EngineStatusButton extends BaseButton {

	public static final String ACTION = "EngineStatusButton";

	private Activity activity;

	public EngineStatusButton(Activity activity) {
		this.activity = activity;
	}

	@Override
	public String getAction() {
		return ACTION;
	}

	@Override
	public void onClick(Button button) {
		activity.getFragmentManager().beginTransaction()
			.setCustomAnimations(R.anim.homescreen_slide_out_up, R.anim.homescreen_slide_in_up)
			.replace(R.id.home_screen_fragment, new EngineStatusFragment(), "engineStatus")
			.addToBackStack(null)
			.commit();
	}

	@Override
	public String getDescription() {
		return "Open Engine Status";
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
