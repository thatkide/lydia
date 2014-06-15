package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.widget.Button;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.Dashboard;
import ca.efriesen.lydia.controllers.ButtonController;
import ca.efriesen.lydia.controllers.ButtonControllers.*;
import ca.efriesen.lydia.databases.*;

import java.util.List;

/**
 * User: eric
 * Date: 2013-03-13
 * Time: 10:00 PM
 */
public class HomeScreenFragment extends Fragment {

	private static final String TAG = "lydia HomeScreen";

	private ButtonController buttonController;

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.home_screen_fragment, container, false);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		Activity activity = getActivity();
		try {
			TextView driverSeatHeat = (TextView) activity.findViewById(R.id.driver_seat_heat);
			savedInstanceState.putInt("driverSeatHeat", driverSeatHeat.getCurrentTextColor());

			TextView passengerSeatHeat = (TextView) activity.findViewById(R.id.passenger_seat_heat);
			savedInstanceState.putInt("passengerSeatHeat", passengerSeatHeat.getCurrentTextColor());

			TextView wiperToggle = (TextView) activity.findViewById(R.id.wiper_toggle);
			savedInstanceState.putInt("wiperToggle", wiperToggle.getCurrentTextColor());
		} catch (Exception e) {}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity activity = getActivity();

		// get the controller and db stuff
		buttonController = new ButtonController(activity);
		ButtonConfigDataSource dataSource = new ButtonConfigDataSource(activity);
		dataSource.open();

		// get the buttons in our area
		List<ca.efriesen.lydia.databases.Button> buttons = dataSource.getButtonsInArea(1);
		// close the db, we don't need it any more
		dataSource.close();

		int numButtons = 6;

		// tell every button to call the button controller, it will decide your fate
		for (int i=0; i<numButtons; i++) {
			// get the resource id for the button
			int resId = getResources().getIdentifier("home" + i, "id", activity.getPackageName());
			// get the button
			Button button = (Button) activity.findViewById(resId);
			button.setOnClickListener(buttonController);
			button.setOnLongClickListener(buttonController);
		}

		// this block ensures we always have a settings button on screen somewhere
		if (!buttonController.hasValidSettingsButton()) {
			// start at 0
			int position = 0;
			// if we have a full screen but no settings
			if (buttons.size() == numButtons) {
				// remove the last button and set it to the settings button
				buttons.remove(position = numButtons-1);
			// we don't have a full screen, but we also don't have a settings button
			} else {
				// loop over all the settings and find the next empty position
				for (ca.efriesen.lydia.databases.Button button : buttons) {
					// if our current selected position is in use, increment it.  the buttons are in position order from sqlite
					if (position == button.getPosition()) {
						position++;
					}
				}
			}
			// Hard code the settings button to always show up if nothing else is on screen
			ca.efriesen.lydia.databases.Button settingsBundle = new ca.efriesen.lydia.databases.Button();
			settingsBundle.setDisplayArea(1);
			settingsBundle.setPosition(position);
			settingsBundle.setTitle(getString(R.string.settings));
			settingsBundle.setAction(SettingsButton.ACTION);
			settingsBundle.setDrawable("settings");
			settingsBundle.setUsesDrawable(true);

			buttons.add(settingsBundle);
		}

		buttonController.populateButton(buttons);

		final Button homeScreenNext = (Button) activity.findViewById(R.id.home_screen_next);
		final Button homeScreenPrev = (Button) activity.findViewById(R.id.home_screen_previous);

		homeScreenNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.controls_slide_in_left, R.anim.controls_slide_in_right)
						.replace(R.id.home_screen_fragment, new HomeScreenTwoFragment(), "homeScreenFragment")
						.addToBackStack(null)
						.commit();
				((Dashboard)activity).setHomeScreenClass(HomeScreenTwoFragment.class);
			}
		});

		homeScreenPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.controls_slide_out_left, R.anim.controls_slide_out_right)
						.replace(R.id.home_screen_fragment, new HomeScreenTwoFragment(), "homeScreenFragment")
						.addToBackStack(null)
						.commit();
				((Dashboard)activity).setHomeScreenClass(HomeScreenTwoFragment.class);
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		buttonController.cleanup();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
//		localBroadcastManager.sendBroadcast(new Intent(MediaService.GET_CURRENT_SONG));
	}
}