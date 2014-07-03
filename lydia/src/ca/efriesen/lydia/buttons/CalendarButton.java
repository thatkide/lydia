package ca.efriesen.lydia.buttons;

import android.app.Activity;
import android.view.View;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.fragments.CalendarView;

/**
 * Created by eric on 2014-06-15.
 */
public class CalendarButton extends BaseButton {

	private Activity activity;

	public CalendarButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(View view, Button button) {
		activity.getFragmentManager().beginTransaction()
			.setCustomAnimations(R.anim.homescreen_slide_out_up, R.anim.homescreen_slide_in_up)
			.replace(R.id.home_screen_fragment, new CalendarView(), "calendarFragment")
			.addToBackStack(null)
			.commit();
	}
}
