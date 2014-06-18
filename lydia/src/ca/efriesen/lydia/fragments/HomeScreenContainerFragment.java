package ca.efriesen.lydia.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ca.efriesen.lydia.R;

/**
 * User: eric
 * Date: 2012-08-18
 * Time: 10:16 PM
 */
public class HomeScreenContainerFragment extends Fragment {

	private static final String TAG = "lydia home screen container";
	private Class homeScreenClass = HomeScreenFragment.class;
	private Class driverControlClass = DriverControlsFragment.class;
	private Class passengerControlClass = PassengerControlsFragment.class;

	// take the class of the screen we want to diaplay in the middle
	public HomeScreenContainerFragment(Class driverControlClass, Class passengerControlClass) {
		// only save the new class if we were passes something other than null
		if (driverControlClass != null)
			this.driverControlClass = driverControlClass;
		if (homeScreenClass != null)
			this.homeScreenClass = homeScreenClass;
		if(passengerControlClass != null)
			this.passengerControlClass = passengerControlClass;
	}

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		setRetainInstance(true);
	}

	// default to home screen one
	public HomeScreenContainerFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.home_screen_container, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		try {
			// setup the home screen
			getFragmentManager().beginTransaction()
					.replace(R.id.driver_controls, (Fragment) driverControlClass.newInstance(), "driverControls")
					.replace(R.id.home_screen_fragment, (Fragment) homeScreenClass.newInstance(), "homeScreenFragment")
					.replace(R.id.passenger_controls, (Fragment) passengerControlClass.newInstance(), "passengerControls")
					.commit();
		} catch (java.lang.InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}
}
