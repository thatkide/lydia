package ca.efriesen.lydia.buttons;

import android.app.Activity;

import ca.efriesen.lydia.R;

/**
 * Created by eric on 2014-06-14.
 */
public class AirRideButton extends BaseButton {
	public static final String ACTION = "AirRideButton";
	private Activity activity;

	public AirRideButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public String getAction() {
		return ACTION;
	}

	@Override
	public String getDescription() {
		return "Open Air Ride Screen";
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
