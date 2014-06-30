package ca.efriesen.lydia.controllers.ButtonControllers;

import android.app.Activity;

/**
 * Created by eric on 2014-06-14.
 */
public class AirRideButton extends BaseButton {

	public static final String ACTION = "AirRideButton";

	public AirRideButton(Activity activity) {
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
