package com.autosenseapp.buttons.appButtons;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.autosenseapp.activities.WebActivity;
import com.autosenseapp.buttons.BaseButton;
import com.autosenseapp.databases.Button;

/**
 * Created by eric on 2014-06-14.
 */
public class ChromeButton extends BaseButton {

	private Activity activity;

	public ChromeButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(View view, Button button) {
		activity.startActivity(new Intent(activity, WebActivity.class));
	}

}
