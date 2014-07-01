package ca.efriesen.lydia.buttons;

import android.app.Activity;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.fragments.WeatherFragment;

/**
 * Created by eric on 2014-06-15.
 */
public class WeatherButton extends BaseButton {

	public static final String ACTION = "WeatherButton";

	private Activity activity;

	public WeatherButton(Activity activity) {
		super(activity);
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
			.replace(R.id.home_screen_fragment, new WeatherFragment(), "weatherFragment")
			.addToBackStack(null)
			.commit();
	}

	@Override
	public String getDescription() {
		return "Open Weather";
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
