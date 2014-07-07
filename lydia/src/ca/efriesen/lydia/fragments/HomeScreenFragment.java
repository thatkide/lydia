package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.Button;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.callbacks.DrawScreenCallback;
import ca.efriesen.lydia.activities.settings.HomeScreenEditorActivity;
import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.buttons.SettingsButton;
import ca.efriesen.lydia.controllers.ButtonController;

import java.util.List;

/**
 * User: eric
 * Date: 2013-03-13
 * Time: 10:00 PM
 */
public class HomeScreenFragment extends Fragment implements View.OnClickListener, DrawScreenCallback {

	private static final String TAG = "lydia HomeScreen";

	private int selectedScreen;
	private int numScreens;
	private ButtonController buttonController;
	private SharedPreferences sharedPreferences;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.home_screen_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Activity activity = getActivity();

		// get the controller and db stuff
		buttonController = new ButtonController(this, HomeScreenEditorActivity.BASENAME, BaseButton.TYPE_HOMESCREEN, BaseButton.GROUP_USER);

		// we'll store basic info in shared prefs, and more complicated info in sqlite
		sharedPreferences = activity.getSharedPreferences(activity.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		numScreens = buttonController.getNumScreens();
		selectedScreen = sharedPreferences.getInt("selectedScreen", 0);

		// tell every button to call the button controller, it will decide your fate
		for (int i=0; i<BaseButton.BUTTONS_PER_HOMESCREEN; i++) {
			// get the resource id for the button
			int resId = getResources().getIdentifier(HomeScreenEditorActivity.BASENAME + i, "id", activity.getPackageName());
			// get the button
			Button button = (Button) activity.findViewById(resId);
			button.setOnClickListener(buttonController);
			button.setOnLongClickListener(buttonController);
		}

		buttonController.drawScreen(selectedScreen);

		Button homeScreenNext = (Button) activity.findViewById(R.id.home_screen_next);
		Button homeScreenPrev = (Button) activity.findViewById(R.id.home_screen_previous);
		RadioGroup radioGroup = (RadioGroup) activity.findViewById(R.id.homescreen_radio_group);

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

		homeScreenNext.setTag(BaseButton.BUTTON_NEXT);
		homeScreenNext.setOnClickListener(buttonController);

		homeScreenPrev.setTag(BaseButton.BUTTON_PREV);
		homeScreenPrev.setOnClickListener(buttonController);
	}

	private void drawFragment(boolean direction) {
		HomeScreenFragment fragment = new HomeScreenFragment();
		getFragmentManager().beginTransaction()
				.setCustomAnimations((direction ? R.anim.controls_slide_in_left : R.anim.controls_slide_out_left), (direction ? R.anim.controls_slide_in_right : R.anim.controls_slide_out_right))
				.replace(R.id.home_screen_fragment, fragment, "homeScreenFragment")
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void onClick(View view) {
		if (view.getTag() instanceof Integer) {
			sharedPreferences.edit().putInt("selectedScreen", buttonController.getSelectedScreen()).apply();
			switch ((Integer) view.getTag()) {
				case BaseButton.BUTTON_NEXT: {
					drawFragment(true);
					break;
				}
				case BaseButton.BUTTON_PREV: {
					drawFragment(false);
					break;
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		buttonController.cleanup();
	}

	@Override
	public void drawScreen(List<ca.efriesen.lydia.databases.Button> buttons) {
		// this block ensures we always have a settings button on screen somewhere
		if (!buttonController.hasValidSettingsButton() && selectedScreen == 0) {
			// start at 0
			int position = 0;
			// if we have a full screen but no settings
			if (buttons.size() == BaseButton.BUTTONS_PER_HOMESCREEN) {
				// remove the last button and set it to the settings button
				buttons.remove(position = BaseButton.BUTTONS_PER_HOMESCREEN-1);
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
			ca.efriesen.lydia.databases.Button settingsButton = new ca.efriesen.lydia.databases.Button();
			settingsButton.setDisplayArea(0);
			settingsButton.setPosition(position);
			settingsButton.setTitle(getString(R.string.settings));
			settingsButton.setAction(SettingsButton.class.getSimpleName());
			settingsButton.setDrawable("settings");
			settingsButton.setUsesDrawable(true);

			buttons.add(settingsButton);
		}
	}

	@Override
	public boolean fullDraw() {
		return false;
	}
}