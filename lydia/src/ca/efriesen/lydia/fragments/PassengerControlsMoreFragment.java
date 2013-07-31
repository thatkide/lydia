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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.ContactList;
import ca.efriesen.lydia.activities.Dashboard;
import ca.efriesen.lydia_common.includes.Intents;

public class PassengerControlsMoreFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.passenger_controls_more_fragment, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		final Activity activity = getActivity();

		Button back = (Button) activity.findViewById(R.id.passenger_back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.controls_slide_in_down, R.anim.controls_slide_out_down)
						.replace(R.id.passenger_controls, new PassengerControlsFragment())
						.addToBackStack(null)
						.commit();
				((Dashboard)getActivity()).setPassengerControlsClass(PassengerControlsFragment.class);
			}
		});

		Button rearDefroster = (Button) activity.findViewById(R.id.rear_window_defrost);
		rearDefroster.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// get the text view of the button clicked
				TextView text = (TextView) activity.findViewById(R.id.rear_window_defrost);
				// make a new intent
				Intent defroster = new Intent(Intents.DEFROSTER);
				// switch based on current color
				switch (text.getCurrentTextColor()) {
					case Color.GREEN: {
						// it's on, so turn it off
						text.setTextColor(Color.WHITE);
						defroster.putExtra("state", false);
						break;
					}
					// it's off, so turn it on
					case Color.WHITE:
					default: {
						text.setTextColor(Color.GREEN);
						defroster.putExtra("state", true);
						break;
					}
				}
				// send the intent
				activity.sendBroadcast(defroster);
			}
		});
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// restore the button states
		if (savedInstanceState != null) {
			TextView defroster = (TextView) getActivity().findViewById(R.id.rear_window_defrost);
			defroster.setTextColor(savedInstanceState.getInt("defroster"));
		}
	}

//	@Override
//	public void onSaveInstanceState(Bundle savedInstanceState) {
//		super.onSaveInstanceState(savedInstanceState);
//		Log.d("lydia", "on save instance in passenger");
//		// save the states of all the buttons on screen
//		TextView defroster = (TextView) getActivity().findViewById(R.id.rear_window_defrost);
//		savedInstanceState.putInt("defroster", defroster.getCurrentTextColor());
//	}

}
