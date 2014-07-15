package ca.efriesen.lydia.buttons.navButtons;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.AddressSearch;
import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.fragments.MapFragment;

/**
 * Created by eric on 2014-07-06.
 */
public class NavSearchButton extends BaseButton {

	private Activity activity;

	public NavSearchButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(View view, Button passed) {
		// get the map fragment, and start the search for result, this will pass the results to the fragment
		activity.getFragmentManager().findFragmentById(R.id.home_screen_fragment).startActivityForResult(new Intent(activity, AddressSearch.class), MapFragment.ADDRESS_SEARCH);
	}

}
