package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.*;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.PlaceDetails;
import ca.efriesen.lydia.includes.GMapV2Direction;
import ca.efriesen.lydia.includes.MapHelpers;
import ca.efriesen.lydia_common.includes.Intents;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import org.w3c.dom.Document;

import java.util.ArrayList;

/**
 * User: eric
 * Date: 2013-05-18
 * Time: 4:17 PM
 */
public class MyMapFragment extends MapFragment implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		GoogleMap.OnMapClickListener,
		GoogleMap.OnMapLongClickListener,
		LocationSource.OnLocationChangedListener,
		GoogleMap.OnCameraChangeListener {


	private Activity activity;
	private GoogleMap map;
	private PolylineOptions rectline;

	private boolean traffic = true;
	private boolean navigating = false;
	private float cameraZoom = 16; // default zoom level

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private LocationClient locationClient;

	private Location currentLocation;

	private static final String TAG = "lydia map fragment";

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		activity = getActivity();
		// create a new location client
		locationClient = new LocationClient(getActivity().getApplicationContext(), this, this);

		MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment);
		try {
			map = mapFragment.getMap();

			map.setMyLocationEnabled(true);
			map.setTrafficEnabled(traffic);
			map.setOnMapClickListener(this);
			map.setOnMapLongClickListener(this);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}

		activity.registerReceiver(drawMarkerReceiver, new IntentFilter(Intents.DRAWMARKER));
		activity.registerReceiver(getDirectionsReceiver, new IntentFilter(Intents.GETDIRECTIONS));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			activity.unregisterReceiver(drawMarkerReceiver);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		try {
			activity.unregisterReceiver(getDirectionsReceiver);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		final FragmentManager manager = getFragmentManager();

		Button map = (Button) getActivity().findViewById(R.id.navigation);
		map.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				manager.beginTransaction()
						// replace the 'dashboard_container' fragment with a new 'settings fragment'
						.hide(manager.findFragmentById(R.id.home_screen_container_fragment))
						.hide(manager.findFragmentById(R.id.settings_fragment))
						.show(manager.findFragmentById(R.id.map_container_fragment))
						.addToBackStack(null)
						.commit();
			}
		});
	}

	@Override
	public void onConnected(Bundle data) {
		currentLocation = locationClient.getLastLocation();
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), cameraZoom));
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(getActivity().getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace();
			}
		} else {
			Log.d(TAG, "Connection failed");
		}
	}

	public void onFragmentVisible() {
		locationClient.connect();
	}

	public void onFragmentHidden() {
		locationClient.disconnect();
	}

	public void drawMarker(final Address address) {
		Marker marker = map.addMarker(new MarkerOptions()
				.position(new LatLng(address.getLatitude(), address.getLongitude()))
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
				.title(address.getFeatureName()));
		marker.showInfoWindow();
		map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				Activity activity = getActivity();
				activity.startActivity(new Intent(activity, PlaceDetails.class).putExtra("address", address));
			}
		});
	}

	@Override
	public void onMapClick(LatLng latLng) {
	}

	@Override
	public void onMapLongClick(LatLng latLng) {
		Marker marker = map.addMarker(new MarkerOptions()
				.position(new LatLng(latLng.latitude, latLng.longitude))
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
				.title(getString(R.string.loading_address)));
		marker.showInfoWindow();

		Address address = MapHelpers.getAddressFromLatLng(latLng);
		drawMarker(address);
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), cameraZoom));
	}

	public boolean toggleTraffic() {
		traffic = !traffic;
		map.setTrafficEnabled(traffic);
		return traffic;
	}

	public void clearMap() {
		map.clear();
		navigating = false;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (navigating) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), cameraZoom));
		}
	}

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		cameraZoom = cameraPosition.zoom;
	}

	private class NavigationTask extends AsyncTask<Address, Void, Address> {
		@Override
		protected Address doInBackground(Address... addresses) {
			Address address = addresses[0];
			LatLng fromPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
			rectline = new PolylineOptions().width(10).color(Color.RED);//Color.rgb(51, 181, 229));

			GMapV2Direction md = new GMapV2Direction();
			Document doc = md.getDocument(fromPosition, new LatLng(address.getLatitude(), address.getLongitude()), GMapV2Direction.MODE_DRIVING);

			ArrayList<LatLng> directionPoint = md.getDirection(doc);

			for (int i=0; i<directionPoint.size(); i++) {
				rectline.add(directionPoint.get(i));
			}
			return address;
		}

		@Override
		protected void onPostExecute(Address address) {
			map.addPolyline(rectline);
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 18));
			map.setTrafficEnabled(false);

			drawMarker(address);
		}
	}


	private BroadcastReceiver drawMarkerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Address address = intent.getParcelableExtra("address");
			drawMarker(address);
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), cameraZoom));
		}
	};

	private BroadcastReceiver getDirectionsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Address address = intent.getParcelableExtra("address");
			map.clear();
			navigating = true;
			new NavigationTask().execute(address);
		}
	};

}