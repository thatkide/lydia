package com.autosenseapp.buttons.settingsButtons;

import android.app.Activity;
import android.app.FragmentManager;
import android.view.View;
import com.autosenseapp.R;
import com.autosenseapp.buttons.BaseButton;
import com.autosenseapp.databases.Button;
import com.autosenseapp.fragments.Settings.ArduinoSettingsFragment;

/**
 * Created by eric on 2014-07-06.
 */
public class ArduinoSettingsButton extends BaseButton {
	private Activity activity;

	public ArduinoSettingsButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(View view, Button passed) {
		FragmentManager manager = activity.getFragmentManager();
		// start loading the settings fragment
		manager.beginTransaction()
				.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
				.replace(R.id.home_screen_fragment, new ArduinoSettingsFragment())
				.addToBackStack(null)
				.commit();
	}
}
