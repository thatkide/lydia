package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.callbacks.FragmentAnimationCallback;
import ca.efriesen.lydia.callbacks.FragmentOnBackPressedCallback;

/**
 * Created by eric on 2013-07-12.
 */
public class MapContainerFragment extends Fragment implements FragmentAnimationCallback, FragmentOnBackPressedCallback {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.map_container_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		getFragmentManager().beginTransaction()
				.add(R.id.map_controls, new MapControlsFragment())
				.add(R.id.map_fragment, new MyMapFragment())
				.commit();
	}

	@Override
	public void onBackPressed() {
		Activity activity = getActivity();
		activity.findViewById(R.id.passenger_controls).setVisibility(View.VISIBLE);
		PassengerControlsFragment fragment = (PassengerControlsFragment) activity.getFragmentManager().findFragmentById(R.id.passenger_controls);
		fragment.showFragment(this);
	}

	@Override
	public void animationComplete(int direction) {
		FragmentManager manager = getFragmentManager();
		manager.beginTransaction()
				.setCustomAnimations(R.anim.container_slide_in_down, R.anim.container_slide_out_down)
				.replace(R.id.home_screen_fragment, new HomeScreenFragment())
				.addToBackStack(null)
				.commit();

		manager.beginTransaction()
				.setCustomAnimations(R.anim.container_slide_in_down, R.anim.container_slide_out_down)
				.replace(R.id.driver_controls, new DriverControlsFragment())
				.addToBackStack(null)
				.commit();
	}
}
