package ca.efriesen.lydia.controllers.ButtonControllers;

import android.app.Activity;
import android.content.Intent;
import ca.efriesen.lydia.activities.WebActivity;

/**
 * Created by eric on 2014-06-14.
 */
public class ChromeButton extends MyButton {

	public static final String ACTION = "ChromeButton";

	private Activity activity;

	public ChromeButton(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onClick() {
		activity.startActivity(new Intent(activity, WebActivity.class));
	}

}