package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.Dashboard;
import ca.efriesen.lydia.controllers.ButtonController;
import ca.efriesen.lydia.controllers.ButtonControllers.*;
import java.util.ArrayList;

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

		int numButtons = 6;
		buttonController = new ButtonController(activity);

		for (int i=0; i<numButtons; i++) {
			// get the resource id for the button
			int resId = getResources().getIdentifier("home" + i, "id", activity.getPackageName());
			// get the button
			Button button = (Button) activity.findViewById(resId);
			button.setOnClickListener(buttonController);
			button.setOnLongClickListener(buttonController);
		}

		Bundle navBundle = new Bundle();
		navBundle.putString("title", getString(R.string.navigation));
		navBundle.putString("drawable", "compass");
		navBundle.putString("action", NavigationButton.ACTION);

		Bundle musicBundle = new Bundle();
		musicBundle.putString("title", getString(R.string.music));
		musicBundle.putString("drawable", "vinyl");
		musicBundle.putString("action", MusicButton.ACTION);

		Bundle airRideBundle = new Bundle();
		airRideBundle.putString("title", getString(R.string.air_ride));
		airRideBundle.putString("drawable", "jet_engine");
		airRideBundle.putString("action", AirRideButton.ACTION);

		Bundle phoneBundle = new Bundle();
		phoneBundle.putString("title", getString(R.string.phone));
		phoneBundle.putString("drawable", "phone");
		phoneBundle.putString("action", PhoneButton.ACTION);

		Bundle androidBundle = new Bundle();
		androidBundle.putString("title", getString(R.string.all_apps));
		androidBundle.putString("drawable", "android");
		androidBundle.putString("action", AndroidButton.ACTION);

		Bundle chromeBundle = new Bundle();
		chromeBundle.putString("title", getString(R.string.chrome));
		chromeBundle.putString("drawable", "chrome");
		chromeBundle.putString("action", ChromeButton.ACTION);

		ArrayList<Bundle> buttons = new ArrayList<Bundle>();
		buttons.add(navBundle);
		buttons.add(musicBundle);
		buttons.add(airRideBundle);
		buttons.add(phoneBundle);
		buttons.add(androidBundle);
		buttons.add(chromeBundle);

		// loop over all buttons
		for (int i=0; i<buttons.size(); i++) {
			// get the bundle of info
			Bundle bundle = buttons.get(i);
			// get the resource id for the button
			int resId = getResources().getIdentifier("home" + i, "id", activity.getPackageName());
			// get the button
			Button button = (Button) activity.findViewById(resId);
			// set the text to the proper title
			button.setText(bundle.getString("title"));
			// get the image resource id
			int imgId = getResources().getIdentifier(bundle.getString("drawable"), "drawable", activity.getPackageName());
			// get the drawable
			Drawable img = activity.getResources().getDrawable(imgId);
			// set it to the top on the button
			button.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
			button.setTag(bundle);
		}

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
