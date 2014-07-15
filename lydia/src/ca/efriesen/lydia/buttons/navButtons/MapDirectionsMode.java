package ca.efriesen.lydia.buttons.navButtons;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.NavigationMode;
import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.fragments.MapFragment;

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
