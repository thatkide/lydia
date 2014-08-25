package com.autosenseapp.buttons.appButtons;

import android.app.Activity;
import android.view.View;

import com.autosenseapp.buttons.BaseButton;
import com.autosenseapp.databases.Button;

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
