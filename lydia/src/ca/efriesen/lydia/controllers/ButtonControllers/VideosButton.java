package ca.efriesen.lydia.controllers.ButtonControllers;

import android.app.Activity;

/**
 * Created by eric on 2014-06-15.
 */
public class VideosButton extends BaseButton {

	public static final String ACTION = "VideosButton";

	private Activity activity;

	public VideosButton(Activity activity) {
		this.activity = activity;
	}

	@Override
	public String getAction() {
		return ACTION;
	}

	@Override
	public String getDescription() {
		return "Opens Videos.  Currently does nothing";
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
