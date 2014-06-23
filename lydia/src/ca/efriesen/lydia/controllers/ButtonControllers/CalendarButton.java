package ca.efriesen.lydia.controllers.ButtonControllers;

import android.app.Activity;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.fragments.CalendarView;

/**
 * Created by eric on 2014-06-15.
 */
public class CalendarButton extends BaseButton {

	public static final String ACTION = "CalendarButton";

	private Activity activity;

	public CalendarButton(Activity activity) {
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
			.replace(R.id.home_screen_fragment, new CalendarView(), "calendarFragment")
			.addToBackStack(null)
			.commit();
	}

	@Override
	public String getDescription() {
		return "Open Calendar";
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
