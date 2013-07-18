package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.AddressSearch;

/**
 * Created by eric on 2013-07-12.
 */
public class MapControlsFragment extends Fragment {

	private Activity activity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.map_controls_fragment, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		final FragmentManager manager = getFragmentManager();
		manager.beginTransaction()
				// hide the map fragment
				.hide(manager.findFragmentById(R.id.map_container_fragment))
				.commit();
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		activity = getActivity();
		final Button search = (Button) activity.findViewById(R.id.map_search);
		final Button toggleTraffic = (Button) activity.findViewById(R.id.toggle_traffic);
		final Button clearMap = (Button) activity.findViewById(R.id.clear_map);
		final MyMapFragment mapFragment = (MyMapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);

		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				activity.startActivity(new Intent(activity, AddressSearch.class));
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
	}
}
