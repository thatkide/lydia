package com.autosenseapp.buttons.navButtons;

import android.app.Activity;
import android.view.View;

import com.autosenseapp.R;
import com.autosenseapp.buttons.BaseButton;
import com.autosenseapp.databases.Button;
import com.autosenseapp.fragments.MapFragment;

/**
 * Created by eric on 2014-07-06.
 */
public class ToggleTrafficButton extends BaseButton {

	private Activity activity;

	public ToggleTrafficButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(View view, Button passed) {
		MapFragment mapFragment = (MapFragment) activity.getFragmentManager().findFragmentById(R.id.home_screen_fragment);
		mapFragment.toggleTraffic();
	}
}
