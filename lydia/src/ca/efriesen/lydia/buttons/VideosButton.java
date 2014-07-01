package ca.efriesen.lydia.buttons;

import android.app.Activity;
import android.view.View;

import ca.efriesen.lydia.databases.Button;

/**
 * Created by eric on 2014-06-15.
 */
public class VideosButton extends BaseButton {

	private Activity activity;

	public VideosButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(View view, Button button) {

	}
}
