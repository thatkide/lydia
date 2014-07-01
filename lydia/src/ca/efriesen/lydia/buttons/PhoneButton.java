package ca.efriesen.lydia.buttons;

import android.app.Activity;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.fragments.PhoneFragment;

/**
 * Created by eric on 2014-06-14.
 */
public class PhoneButton extends BaseButton {

	public static final String ACTION = "PhoneButton";

	private Activity activity;

	public PhoneButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(Button button) {
		activity.getFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.homescreen_slide_out_up, R.anim.homescreen_slide_in_up)
				.replace(R.id.home_screen_fragment, new PhoneFragment(), "phoneFragment")
				.addToBackStack(null)
				.commit();
	}

	@Override
	public String getAction() {
		return ACTION;
	}

	@Override
	public String getDescription() {
		return "Open Phone Controls";
	}

	@Override
	public String toString() {
		return getDescription();
	}

}
