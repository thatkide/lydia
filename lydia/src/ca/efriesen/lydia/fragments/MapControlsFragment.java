package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.AddressSearch;
import ca.efriesen.lydia.activities.NavigationMode;
import ca.efriesen.lydia.includes.GMapV2Direction;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;


/**
 * Created by eric on 2013-07-12.
 */
public class MapControlsFragment extends Fragment {

	private static final String TAG = "lydia map controls";
	private Activity activity;

	private static final int NAV_MODE = 1;
	private static final int ADDRESS_SEARCH = 2;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.map_controls_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		activity = getActivity();
		final Button search = (Button) activity.findViewById(R.id.map_search);
		final Button toggleTraffic = (Button) activity.findViewById(R.id.toggle_traffic);
		final Button clearMap = (Button) activity.findViewById(R.id.clear_map);
		final Button directionsMode = (Button) activity.findViewById(R.id.map_directions_mode);
		final MyMapFragment mapFragment = (MyMapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);

		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivityForResult(new Intent(activity, AddressSearch.class), ADDRESS_SEARCH);
			}
		});
		toggleTraffic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mapFragment.toggleTraffic();
			}
		});
		clearMap.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mapFragment.clearMap();
			}
		});

		directionsMode.setText(GMapV2Direction.getModes().get(mapFragment.getDirectionsMode()));
		directionsMode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// start the navigation mode activity
				startActivityForResult(new Intent(activity, NavigationMode.class), NAV_MODE);
			}
		});

	}

	// if the user selects a different mode in the nav activity
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		MyMapFragment mapFragment = (MyMapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
		// check the codes
		switch (requestCode) {
			case NAV_MODE: {
				if (resultCode == Activity.RESULT_OK) {
					// all checks out, get the map and button views
					Button directionsMode = (Button) activity.findViewById(R.id.map_directions_mode);

					// set the directions in the map class
					mapFragment.setDirectionsMode(intent.getIntExtra("mode", 0));
					// change the text in the side bar
					directionsMode.setText(GMapV2Direction.getModes().get(mapFragment.getDirectionsMode()));
				}
				break;
			}
			case ADDRESS_SEARCH: {
				if (resultCode == Activity.RESULT_OK) {
					Address address = intent.getParcelableExtra("address");
					mapFragment.drawMarker(address, CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), mapFragment.getCameraZoom()));
				}
				break;
			}
		}
	}
}
