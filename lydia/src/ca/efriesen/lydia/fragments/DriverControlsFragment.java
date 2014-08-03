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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import java.util.List;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.buttons.appButtons.SettingsButton;
import ca.efriesen.lydia.callbacks.DrawScreenCallback;
import ca.efriesen.lydia.activities.settings.SidebarEditorActivity;
import ca.efriesen.lydia.callbacks.FragmentAnimationCallback;
import ca.efriesen.lydia.controllers.ButtonController;
import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.databases.Button;

public class DriverControlsFragment extends Fragment implements View.OnClickListener, DrawScreenCallback {

	private static final String TAG = "driver controls";

	private Activity activity;
	private int selectedScreen = 0;
	private ButtonController buttonController;
	private int group = BaseButton.GROUP_USER;

	private ImageButton driverUp;
	private ImageButton driverDown;
	private LinearLayout driverAdminNavGroup;
	private final String selectedDriverBar = "selectedDriverBar";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.driver_controls_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = getActivity();
		if (getArguments() != null) {
			selectedScreen = getArguments().getInt(selectedDriverBar, 0);
			group = getArguments().getInt("group");
		}

		// get the controller and db stuff
		buttonController = new ButtonController(this, SidebarEditorActivity.DRIVERBASENAME, BaseButton.TYPE_SIDEBAR_LEFT, group);

		driverUp = (ImageButton) activity.findViewById(R.id.driver_up);
		driverDown = (ImageButton) activity.findViewById(R.id.driver_down);
		ImageButton driverUp2 = (ImageButton) activity.findViewById(R.id.driver_up_2);
		ImageButton driverDown2 = (ImageButton) activity.findViewById(R.id.driver_down_2);
		driverAdminNavGroup = (LinearLayout) activity.findViewById(R.id.driver_nav_group);

		// tell every button to call the button controller, it will decide your fate
		for (int i=0; i<BaseButton.BUTTONS_PER_SIDEBAR; i++) {
			// get the resource id for the button
			int resId = getResources().getIdentifier(SidebarEditorActivity.DRIVERBASENAME + i, "id", activity.getPackageName());
			// get the button
			android.widget.Button button = (android.widget.Button) activity.findViewById(resId);
			button.setOnClickListener(buttonController);
			button.setOnLongClickListener(buttonController);
		}

		buttonController.drawScreen(selectedScreen);

		// set tag so the controller knows what the button is for
		driverUp.setTag(BaseButton.BUTTON_NEXT);
		driverUp.setOnClickListener(buttonController);
		driverUp2.setTag(BaseButton.BUTTON_NEXT);
		driverUp2.setOnClickListener(buttonController);
		driverDown.setTag(BaseButton.BUTTON_PREV);
		driverDown.setOnClickListener(buttonController);
		driverDown2.setTag(BaseButton.BUTTON_PREV);
		driverDown2.setOnClickListener(buttonController);
	}

	public int getGroup() {
		return group;
	}

	private void drawFragment(boolean direction) {
		DriverControlsFragment fragment = new DriverControlsFragment();
		Bundle args = new Bundle();
		args.putInt(selectedDriverBar, buttonController.getSelectedScreen());
		args.putInt("group", group);
		fragment.setArguments(args);
		getFragmentManager().beginTransaction()
				.setCustomAnimations((!direction ? R.anim.controls_slide_in_down : R.anim.controls_slide_out_up), (!direction ? R.anim.controls_slide_out_down : R.anim.controls_slide_in_up))
				.replace(R.id.driver_controls, fragment)
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void onClick(View view) {
		if (view.getTag() instanceof Integer) {
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
			driverUp.setEnabled(false);
		} else {
			driverUp.setEnabled(true);
		}
		if (numScreens > 1 && selectedScreen == (numScreens -1)) {
			driverDown.setVisibility(View.VISIBLE);
			driverUp.setVisibility(View.GONE);
			driverAdminNavGroup.setVisibility(View.GONE);
		} else if (numScreens > 2 && (selectedScreen != 0) && (selectedScreen != numScreens-1) ) {
			driverAdminNavGroup.setVisibility(View.VISIBLE);
			driverDown.setVisibility(View.GONE);
			driverUp.setVisibility(View.GONE);
		}

		SharedPreferences sharedPreferences = activity.getSharedPreferences(activity.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		boolean showButtons = sharedPreferences.getBoolean("useHomeScreenButtons", true);
		if (!buttonController.hasValidSettingsButton(BaseButton.TYPE_SIDEBAR_LEFT) && selectedScreen == 0 && !showButtons && group == BaseButton.GROUP_USER) {
			// start at 0
			int position = 0;
			// if we have a full screen but no settings
			if (buttons.size() == BaseButton.BUTTONS_PER_SIDEBAR) {
				// remove the last button and set it to the settings button
				buttons.remove(position = BaseButton.BUTTONS_PER_SIDEBAR - 1);
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
			settingsButton.setUsesDrawable(false);

			buttons.add(settingsButton);
		}
	}

	@Override
	public boolean fullDraw() {
		return false;
	}

	public void showFragment(final Object object) {
		Animation animation = AnimationUtils.loadAnimation(activity, R.anim.slide_in_right);

		View driverControls = activity.findViewById(R.id.driver_controls);
		driverControls.startAnimation(animation);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if (object instanceof FragmentAnimationCallback) {
					((FragmentAnimationCallback) object).animationStart(FragmentAnimationCallback.SHOW);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// reshow the controls fragment
				activity.findViewById(R.id.driver_controls).setVisibility(View.VISIBLE);

				if (object instanceof FragmentAnimationCallback) {
					((FragmentAnimationCallback) object).animationComplete(FragmentAnimationCallback.SHOW);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		});
	}

	public void hideFragment(final Object object) {
		View homescreen = activity.findViewById(R.id.map_container);
		View driverControls = activity.findViewById(R.id.driver_controls);

		Animation controlsAnim = AnimationUtils.loadAnimation(activity, R.anim.slide_out_left);

//		ResizeWidthAnimation animation = new ResizeWidthAnimation(homescreen, homescreen.getWidth()+driverControls.getWidth());
		// set the proper duration
//		animation.setDuration(controlsAnim.getDuration());
//		homescreen.startAnimation(animation);

		driverControls.startAnimation(controlsAnim);
		controlsAnim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if (object instanceof FragmentAnimationCallback) {
					((FragmentAnimationCallback) object).animationStart(FragmentAnimationCallback.HIDE);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (object instanceof FragmentAnimationCallback) {
					((FragmentAnimationCallback) object).animationComplete(FragmentAnimationCallback.HIDE);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		});
	}
}