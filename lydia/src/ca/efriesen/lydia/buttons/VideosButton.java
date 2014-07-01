package ca.efriesen.lydia.buttons;

import android.app.Activity;

/**
 * Created by eric on 2014-06-15.
 */
public class VideosButton extends BaseButton {

	public static final String ACTION = "VideosButton";

	private Activity activity;

	public VideosButton(Activity activity) {
		super(activity);
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
