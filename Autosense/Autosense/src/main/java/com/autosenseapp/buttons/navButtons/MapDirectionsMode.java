package com.autosenseapp.buttons.navButtons;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import com.autosenseapp.R;
import com.autosenseapp.activities.NavigationMode;
import com.autosenseapp.buttons.BaseButton;
import com.autosenseapp.databases.Button;
import com.autosenseapp.fragments.MapFragment;

/**
 * Created by eric on 2014-07-06.
 */
public class MapDirectionsMode extends BaseButton {

	private Activity activity;

	public MapDirectionsMode(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(View view, Button passed) {
		activity.getFragmentManager().findFragmentById(R.id.home_screen_fragment).startActivityForResult(new Intent(activity, NavigationMode.class).putExtra("button", passed), MapFragment.NAV_MODE);
	}
}
