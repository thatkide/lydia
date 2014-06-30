package ca.efriesen.lydia.fragments;

/**
 * User: eric
 * Date: 2013-03-13
 * Time: 10:00 PM
 */
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.List;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.settings.DrawScreenCallback;
import ca.efriesen.lydia.activities.settings.SidebarEditorActivity;
import ca.efriesen.lydia.controllers.ButtonController;
import ca.efriesen.lydia.controllers.ButtonControllers.BaseButton;
import ca.efriesen.lydia.databases.Button;

public class PassengerControlsFragment extends Fragment implements View.OnClickListener, DrawScreenCallback {

	private static final String TAG = "passenger controls";

	private int selectedScreen;
	private ButtonController buttonController;
	SharedPreferences sharedPreferences;

	private ImageButton passengerUp;
	private ImageButton passengerDown;
	private LinearLayout passengerAdminNavGroup;
	private final String selectedPassengerBar = "selectedPassengerBar";

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.passenger_controls_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Activity activity = getActivity();

		// get the controller and db stuff
		buttonController = new ButtonController(this, SidebarEditorActivity.PASSENGERBASENAME, BaseButton.TYPE_SIDEBAR_RIGHT);

		// we'll store basic info in shared prefs, and more complicated info in sqlite
		sharedPreferences = activity.getSharedPreferences(activity.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		selectedScreen = sharedPreferences.getInt(selectedPassengerBar, 0);

		passengerUp = (ImageButton) activity.findViewById(R.id.passenger_up);
		passengerDown = (ImageButton) activity.findViewById(R.id.passenger_down);
		ImageButton passengerUp2 = (ImageButton) activity.findViewById(R.id.passenger_up_2);
		ImageButton passengerDown2 = (ImageButton) activity.findViewById(R.id.passenger_down_2);
		passengerAdminNavGroup = (LinearLayout) activity.findViewById(R.id.passenger_nav_group);

		// tell every button to call the button controller, it will decide your fate
		for (int i=0; i< SidebarEditorActivity.numButtons; i++) {
			// get the resource id for the button
			int resId = getResources().getIdentifier(SidebarEditorActivity.PASSENGERBASENAME + i, "id", activity.getPackageName());
			// get the button
			android.widget.Button button = (android.widget.Button) activity.findViewById(resId);
			button.setOnClickListener(buttonController);
			button.setOnLongClickListener(buttonController);
		}

		buttonController.drawScreen(selectedScreen);

		// set tag so the controller knows what the button is for
		passengerUp.setTag(BaseButton.BUTTON_NEXT);
		passengerUp.setOnClickListener(buttonController);
		passengerUp2.setTag(BaseButton.BUTTON_NEXT);
		passengerUp2.setOnClickListener(buttonController);
		passengerDown.setTag(BaseButton.BUTTON_PREV);
		passengerDown.setOnClickListener(buttonController);
		passengerDown2.setTag(BaseButton.BUTTON_PREV);
		passengerDown2.setOnClickListener(buttonController);

	}

	private void drawFragment(boolean direction) {
		PassengerControlsFragment fragment = new PassengerControlsFragment();
		getFragmentManager().beginTransaction()
				.setCustomAnimations((!direction ? R.anim.controls_slide_in_down : R.anim.controls_slide_out_up), (!direction ? R.anim.controls_slide_out_down : R.anim.controls_slide_in_up))
				.replace(R.id.passenger_controls, fragment)
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void onClick(View view) {
		if (view.getTag() instanceof Integer) {
			sharedPreferences.edit().putInt(selectedPassengerBar, buttonController.getSelectedScreen()).apply();
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
	public void drawScreen(List<Button> buttons) {
		int numScreens = buttonController.getNumScreens();

		if (numScreens == 1) {
			passengerUp.setEnabled(false);
		} else {
			passengerUp.setEnabled(true);
		}
		if (numScreens > 1 && selectedScreen == (numScreens -1)) {
			passengerDown.setVisibility(View.VISIBLE);
			passengerUp.setVisibility(View.GONE);
			passengerAdminNavGroup.setVisibility(View.GONE);
		} else if (numScreens > 2 && (selectedScreen != 0) && (selectedScreen != numScreens-1) ) {
			passengerAdminNavGroup.setVisibility(View.VISIBLE);
			passengerDown.setVisibility(View.GONE);
			passengerUp.setVisibility(View.GONE);
		}
	}
}