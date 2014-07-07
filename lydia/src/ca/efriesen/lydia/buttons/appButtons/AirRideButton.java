package ca.efriesen.lydia.buttons.appButtons;

import android.app.Activity;
import android.view.View;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.databases.Button;

/**
 * Created by eric on 2014-06-14.
 */
public class AirRideButton extends BaseButton {
	private Activity activity;

	public AirRideButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(View view, Button button) {

	}
}
