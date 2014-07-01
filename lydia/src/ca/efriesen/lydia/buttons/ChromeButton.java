package ca.efriesen.lydia.buttons;

import android.app.Activity;
import android.content.Intent;
import ca.efriesen.lydia.activities.WebActivity;
import ca.efriesen.lydia.databases.Button;

/**
 * Created by eric on 2014-06-14.
 */
public class ChromeButton extends BaseButton {

	public static final String ACTION = "ChromeButton";

	private Activity activity;

	public ChromeButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(Button button) {
		activity.startActivity(new Intent(activity, WebActivity.class));
	}

	@Override
	public String getAction() {
		return ACTION;
	}

	@Override
	public String getDescription() {
		return "Open Chrome activity";
	}

	@Override
	public String toString() {
		return getDescription();
	}

}
