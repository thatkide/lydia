package ca.efriesen.lydia.buttons.navButtons;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.callbacks.FragmentAnimationCallback;
import ca.efriesen.lydia.fragments.DriverControlsFragment;

/**
 * Created by eric on 2014-07-19.
 */
public class ShowHide extends Button implements FragmentAnimationCallback{

	private static final String TAG = ShowHide.class.getSimpleName();

	private Activity activity;

	public ShowHide(Context context) {
		super(context);
		this.activity = (Activity) context;
	}


	public void onClick(View view, Button passed) {
		DriverControlsFragment fragment = (DriverControlsFragment) activity.getFragmentManager().findFragmentById(R.id.driver_controls);
		if (activity.findViewById(R.id.driver_controls).getVisibility() == View.GONE) {
			fragment.showFragment(this);
		} else {
			fragment.hideFragment(this);
		}
	}

	@Override
	public void animationComplete(int direction) {
		if (direction == HIDE) {
			// remove the controls view from the layout
			activity.findViewById(R.id.driver_controls).setVisibility(View.GONE);
		} else {
			activity.findViewById(R.id.driver_controls).setVisibility(VISIBLE);
		}
	}

	@Override
	public void animationStart(int direction) { }
}
