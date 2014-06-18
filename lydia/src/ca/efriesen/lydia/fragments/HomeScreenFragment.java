package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.widget.Button;
import ca.efriesen.lydia.R;
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

	private Activity activity;
	private int selectedScreen;
	private int numScreens;
	private ButtonController buttonController;
	private RadioGroup radioGroup;

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		try {
			selectedScreen = getArguments().getInt("selectedScreen");
		} catch (Exception e) {}
		return inflater.inflate(R.layout.home_screen_fragment, container, false);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

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
		activity = getActivity();

		// get the controller and db stuff
		buttonController = new ButtonController(activity);

		// we'll store basic info in shared prefs, and more complicated info in sqlite
		final SharedPreferences sharedPreferences = activity.getSharedPreferences(activity.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		numScreens = sharedPreferences.getInt("numHomeScreens", 1);
		selectedScreen = sharedPreferences.getInt("selectedScreen", 0);

		ButtonConfigDataSource dataSource = new ButtonConfigDataSource(activity);
		dataSource.open();

		// get the buttons in our selectedScreen
		List<ca.efriesen.lydia.databases.Button> buttons = dataSource.getButtonsInArea(selectedScreen);
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
		if (!buttonController.hasValidSettingsButton() && selectedScreen == 0) {
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
			settingsBundle.setDisplayArea(0);
			settingsBundle.setPosition(position);
			settingsBundle.setTitle(getString(R.string.settings));
			settingsBundle.setAction(SettingsButton.ACTION);
			settingsBundle.setDrawable("settings");
			settingsBundle.setUsesDrawable(true);

			buttons.add(settingsBundle);
		}

		buttonController.populateButton(buttons);

		Button homeScreenNext = (Button) activity.findViewById(R.id.home_screen_next);
		Button homeScreenPrev = (Button) activity.findViewById(R.id.home_screen_previous);
		radioGroup = (RadioGroup) activity.findViewById(R.id.homescreen_radio_group);

		// draw radio buttons
		for (int i=0; i<numScreens; i++) {
			RadioButton radioButton = new RadioButton(activity);
			radioButton.setId(i);
			if (i == selectedScreen) {
				// decrease the size of the radio buttons
				radioButton.setHeight(35);
				radioButton.setWidth(35);
				radioButton.setChecked(true);
			}
			radioGroup.addView(radioButton);
		}

		homeScreenNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selectedScreen < numScreens-1) {
					selectedScreen++;
				} else {
					selectedScreen = 0;
				}
				sharedPreferences.edit().putInt("selectedScreen", selectedScreen).apply();
				drawFragment(true);
			}
		});

		homeScreenPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selectedScreen > 0) {
					selectedScreen--;
				} else {
					selectedScreen = numScreens-1;
				}
				sharedPreferences.edit().putInt("selectedScreen", selectedScreen).apply();
				drawFragment(false);
			}
		});
	}

	private void drawFragment(boolean direction) {
		Bundle args = new Bundle();
		args.putInt("selectedScreen", selectedScreen);
		HomeScreenFragment fragment = new HomeScreenFragment();
		fragment.setArguments(args);
		getFragmentManager().beginTransaction()
				.setCustomAnimations((direction ? R.anim.controls_slide_in_left : R.anim.controls_slide_out_left), (direction ? R.anim.controls_slide_in_right : R.anim.controls_slide_out_right))
				.replace(R.id.home_screen_fragment, fragment, "homeScreenFragment")
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		buttonController.cleanup();
	}
}