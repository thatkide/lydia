package ca.efriesen.lydia.fragments;

/**
 * User: eric
 * Date: 2013-03-13
 * Time: 10:00 PM
 */
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia_common.includes.Intents;

public class DriverControlsMoreFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.driver_controls_more_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final Activity activity = getActivity();
		final FragmentManager manager = getFragmentManager();

		// hide our self
		manager.beginTransaction().hide(manager.findFragmentById(R.id.driver_controls_more)).commit();

		Button back = (Button) activity.findViewById(R.id.driver_back);

		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Fragment driverControls = manager.findFragmentById(R.id.driver_controls);
				Fragment driverControlsMore = manager.findFragmentById(R.id.driver_controls_more);

				manager.beginTransaction()
				.hide(driverControlsMore)
				.show(driverControls)
				.addToBackStack(null)
				.commit();
			}
		});

		Button wipe = (Button) activity.findViewById(R.id.wipe);
		Button wiperToggle = (Button) activity.findViewById(R.id.wiper_toggle);
		ImageButton wiperDelayLess = (ImageButton) activity.findViewById(R.id.wiper_delay_less);

		wipe.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// wipe the window now
				Intent wipe = new Intent(Intents.WIPE);
				activity.sendBroadcast(wipe);
			}
		});

		wiperDelayLess.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
			}
		});

		wiperToggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// get the text of the button
				TextView text = (TextView) activity.findViewById(view.getId());
				// create a new intent
				Intent wipers = new Intent(Intents.WIPERS);

				// switch based on color
				switch (text.getCurrentTextColor()) {
					// it's green
					case Color.GREEN: {
						// set the text white, and turn off
						text.setTextColor(Color.WHITE);
						wipers.putExtra("state", false);
						// also disable the wiper delay
						Intent delay = new Intent(Intents.WIPERDELAY);
						delay.putExtra("delay", 0);
						// broadcast the delay is off
						activity.sendBroadcast(delay);
						break;
					}
					// default state.  it should be white
					default: {
						// set it green, and turn on
						text.setTextColor(Color.GREEN);
						wipers.putExtra("state", true);
						break;
					}
				}
				// send the intent
				activity.sendBroadcast(wipers);
			}
		});

	}
}
