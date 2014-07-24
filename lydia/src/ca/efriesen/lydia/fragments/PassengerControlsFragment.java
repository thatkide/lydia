package ca.efriesen.lydia.fragments;

/**
 * User: eric
 * Date: 2013-03-13
 * Time: 10:00 PM
 */
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.callbacks.DrawScreenCallback;
import ca.efriesen.lydia.activities.settings.SidebarEditorActivity;
import ca.efriesen.lydia.callbacks.FragmentAnimationCallback;
import ca.efriesen.lydia.controllers.ButtonController;
import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.includes.ResizeWidthAnimation;

public class PassengerControlsFragment extends Fragment implements View.OnClickListener, DrawScreenCallback {

	private static final String TAG = "passenger controls";

	private Activity activity;
	private int selectedScreen;
	private ButtonController buttonController;
	private int group = BaseButton.GROUP_USER;

	private ImageButton passengerUp;
	private ImageButton passengerDown;
	private LinearLayout passengerAdminNavGroup;
	private final String selectedPassengerBar = "selectedPassengerBar";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.passenger_controls_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = getActivity();
		if (getArguments() != null) {
			selectedScreen = getArguments().getInt(selectedPassengerBar);
			group = getArguments().getInt("group");
		}

		// get the controller and db stuff
		buttonController = new ButtonController(this, SidebarEditorActivity.PASSENGERBASENAME, BaseButton.TYPE_SIDEBAR_RIGHT, group);

		passengerUp = (ImageButton) activity.findViewById(R.id.passenger_up);
		passengerDown = (ImageButton) activity.findViewById(R.id.passenger_down);
		ImageButton passengerUp2 = (ImageButton) activity.findViewById(R.id.passenger_up_2);
		ImageButton passengerDown2 = (ImageButton) activity.findViewById(R.id.passenger_down_2);
		passengerAdminNavGroup = (LinearLayout) activity.findViewById(R.id.passenger_nav_group);

		// tell every button to call the button controller, it will decide your fate
		for (int i=0; i<BaseButton.BUTTONS_PER_SIDEBAR; i++) {
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
		Bundle args = new Bundle();
		args.putInt(selectedPassengerBar, buttonController.getSelectedScreen());
		args.putInt("group", group);
		fragment.setArguments(args);

		getFragmentManager().beginTransaction()
				.setCustomAnimations((!direction ? R.anim.controls_slide_in_down : R.anim.controls_slide_out_up), (!direction ? R.anim.controls_slide_out_down : R.anim.controls_slide_in_up))
				.replace(R.id.passenger_controls, fragment)
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

	@Override
	public boolean fullDraw() {
		return false;
	}

	public void showFragment(final Object object) {
		// get the views
		View passengerControls = activity.findViewById(R.id.passenger_controls);
		View homescreen = activity.findViewById(R.id.home_screen_fragment);

		// passenger slide in animation
		Animation controlsAnimation = AnimationUtils.loadAnimation(activity, R.anim.slide_in_left);
		// we resize the home screen fragment, use the custom animation
		ResizeWidthAnimation animation = new ResizeWidthAnimation(homescreen, homescreen.getWidth()-passengerControls.getWidth());
		// set the proper duration
		animation.setDuration(controlsAnimation.getDuration());
		homescreen.startAnimation(animation);
		// start the animation
		passengerControls.startAnimation(controlsAnimation);
		controlsAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if (object instanceof FragmentAnimationCallback) {
					((FragmentAnimationCallback) object).animationStart(FragmentAnimationCallback.SHOW);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// reshow the controls fragment
				activity.findViewById(R.id.passenger_controls).setVisibility(View.VISIBLE);

				if (object instanceof FragmentAnimationCallback) {
					// fire the callback
					((FragmentAnimationCallback) object).animationComplete(FragmentAnimationCallback.SHOW);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) { }
		});
	}

	public void hideFragment(final Object object) {
		Animation animation = AnimationUtils.loadAnimation(activity, R.anim.slide_out_right);
		// reset the homescreen to match parent, ensuring it fills the screen
		View homescreen = activity.findViewById(R.id.home_screen_fragment);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) homescreen.getLayoutParams();
		params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
		homescreen.setLayoutParams(params);
		// start the animation of the controls fragment
		View passengerControls = activity.findViewById(R.id.passenger_controls);
		passengerControls.startAnimation(animation);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if (object instanceof FragmentAnimationCallback) {
					((FragmentAnimationCallback) object).animationStart(FragmentAnimationCallback.HIDE);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (object instanceof FragmentAnimationCallback) {
					// fire the callback
					((FragmentAnimationCallback) object).animationComplete(FragmentAnimationCallback.HIDE);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		});
	}
}