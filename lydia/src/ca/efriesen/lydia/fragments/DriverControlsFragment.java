package ca.efriesen.lydia.fragments;

/**
 * User: eric
 * Date: 2013-03-13
 * Time: 10:00 PM
 */
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.Dashboard;
import ca.efriesen.lydia.devices.Windows;
import ca.efriesen.lydia_common.includes.Constants;
import ca.efriesen.lydia_common.includes.Intents;

public class DriverControlsFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.driver_controls_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final Activity activity = getActivity();

		// get the buttons, and register the listeners
		Button driverWindowUp = (Button) activity.findViewById(R.id.driver_window_up);
		Button driverWindowDown = (Button) activity.findViewById(R.id.driver_window_down);

		driverWindowUp.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				// get the window intent from the windows class.  we send a view, a motion event, and it will return an intent we can then broadcast
				activity.sendBroadcast(Windows.sendWindowCommand(view, motionEvent));
				return true;
			}
		});

		driverWindowDown.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				activity.sendBroadcast(Windows.sendWindowCommand(view, motionEvent));
				return true;
			}
		});

		// get the more button and attach an onclick listener
		Button more = (Button) activity.findViewById(R.id.driver_more);

		more.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// hide the main one and show the new one
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.controls_slide_out_up, R.anim.controls_slide_in_up)
						.replace(R.id.driver_controls, new DriverControlsMoreFragment(), "driverControls")
						.addToBackStack(null)
						.commit();
				((Dashboard)getActivity()).setDriverControlsClass(DriverControlsMoreFragment.class);
			}
		});

		Button seatHeat = (Button) activity.findViewById(R.id.driver_seat_heat);
		seatHeat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get the text view of the pressed button
				TextView text = (TextView) activity.findViewById(R.id.driver_seat_heat);
				// get the current color
				int currentColor = text.getCurrentTextColor();
				// create a new intent to broadcast
				Intent seatHeat = new Intent(Intents.SEATHEAT);
				seatHeat.putExtra("seatId", Constants.DRIVERSEAT);

				// switch through the three options
				switch (currentColor) {
					case Color.YELLOW: {
						text.setTextColor(Color.RED);
						seatHeat.putExtra("temp", 2);
						break;
					}
					case Color.RED: {
						text.setTextColor(Color.WHITE);
						seatHeat.putExtra("temp", 0);
						break;
					}
					default: {
						text.setTextColor(Color.YELLOW);
						seatHeat.putExtra("temp", 1);
						break;
					}
				}
				activity.sendBroadcast(seatHeat);
			}
		});
	}
}
