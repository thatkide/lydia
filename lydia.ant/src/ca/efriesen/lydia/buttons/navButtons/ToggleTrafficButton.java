package ca.efriesen.lydia.buttons.navButtons;

import android.app.Activity;
import android.view.View;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.fragments.MapFragment;

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
