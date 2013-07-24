package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.*;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.PlaceDetails;
import ca.efriesen.lydia.includes.GMapV2Direction;
import ca.efriesen.lydia.includes.Helpers;
import ca.efriesen.lydia.includes.MapHelpers;
import ca.efriesen.lydia_common.includes.Intents;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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

	private static final int PLACE_DETAILS = 1;

	private Activity activity;
	private GoogleMap map;
	private PolylineOptions rectline;
	private int mode = GMapV2Direction.MODE_DRIVING;

	private boolean traffic = true;
	private boolean navigating = false;
	private float cameraZoom = 16; // default zoom level

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private LocationClient locationClient;

	private Location currentLocation;

	private boolean showSelf = false;
	private static final String TAG = "lydia map fragment";

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		activity = getActivity();
		// create a new location client
		locationClient = new LocationClient(getActivity().getApplicationContext(), this, this);

		// get the map fragment
		MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment);
		try {
			// get the actual map
			map = mapFragment.getMap();

			// turn on my location
			map.setMyLocationEnabled(true);
			// set traffic mode
			map.setTrafficEnabled(traffic);
			// set this fragment as the onclick listner
			map.setOnMapClickListener(this);
			map.setOnMapLongClickListener(this);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		locationClient.connect();

		activity.registerReceiver(mapAddressReceiver, new IntentFilter(Intents.SHOWONMAP));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStart() {
		super.onStart();
		final FragmentManager manager = getFragmentManager();

		// map on the homescreen that opens the map fragment
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
	public void onResume() {
		super.onResume();
		if (showSelf) {
			ArrayList<Fragment> fragments = new ArrayList<Fragment>();
			fragments.add(getFragmentManager().findFragmentById(R.id.map_container_fragment));
			Helpers.hideAllFragmentsBut(getFragmentManager(), fragments);
			showSelf = false;
		}
	}

	@Override
	public void onConnected(Bundle data) {
		// when the location provider is connected
		// set the current location to the last known location
		currentLocation = locationClient.getLastLocation();
		// move the camera to that spot
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), cameraZoom));
	}

	@Override
	public void onDisconnected() {
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

	// connect and disconnect on fragment hidden/visible
	public void onFragmentVisible() {
	}

	public void onFragmentHidden() {
//		locationClient.disconnect();
	}

//	make poly line change color after we've pased in in driving

	public void drawMarker(Location location) {
		try {
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			Address address =  new MapHelpers.GetAddressFromLatLngTask().execute(latLng).get();
			// draw the marker
			drawMarker(address);
			// move the camera to that spot
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, cameraZoom));
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public void drawMarker(Address address, CameraUpdate cameraUpdate) {
		drawMarker(address);
		map.animateCamera(cameraUpdate);
	}

	// method that draws a marker based on an address
	public void drawMarker(final Address address) {
		try {
			Marker marker = map.addMarker(new MarkerOptions()
					.position(new LatLng(address.getLatitude(), address.getLongitude()))
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
					.title(address.getFeatureName()));
			marker.showInfoWindow();
			map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
				@Override
				public void onInfoWindowClick(Marker marker) {
					startActivityForResult(new Intent(activity, PlaceDetails.class).putExtra("address", address), PLACE_DETAILS);
				}
			});
		} catch (Exception e) {
			Log.d(TAG, "drawing marker failed", e);
		}
	}

	@Override
	public void onMapClick(LatLng latLng) {
	}

	@Override
	public void onMapLongClick(LatLng latLng) {
		// create a new marker and set the position to the position that was clicked
		Marker marker = map.addMarker(new MarkerOptions()
				.position(new LatLng(latLng.latitude, latLng.longitude))
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
				.title(getString(R.string.loading_address)));
		// show the window
		marker.showInfoWindow();

		// get the address from the latlng pressed
		// this is an async task and will replace the loading marker drawn above
		try {
			Address address =  new MapHelpers.GetAddressFromLatLngTask().execute(latLng).get();
			// draw the marker
			drawMarker(address);
			// move the camera to that spot
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), cameraZoom));
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	// toggle if traffic is displayed or not
	public boolean toggleTraffic() {
		traffic = !traffic;
		map.setTrafficEnabled(traffic);
		return traffic;
	}

	// removes all markers and directions
	public void clearMap() {
		map.clear();
		navigating = false;
		TextView travel_info = (TextView) activity.findViewById(R.id.map_travel_info);
		travel_info.setText("");
	}

	// set the directions mode
	public void setDirectionsMode(int mode) {
		Log.d(TAG, "mode is " + mode);
		this.mode = mode;
	}

	public int getDirectionsMode() {
		return this.mode;
	}

	@Override
	public void onLocationChanged(Location location) {
		// if we're navigating, keep the camera centered on our location
		if (navigating) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), cameraZoom));
		}
	}

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		// save the zoom specified from the zoom in/out buttons
		cameraZoom = cameraPosition.zoom;
	}

	public float getCameraZoom() {
		return cameraZoom;
	}

	// this will get our directions from our current position to the specified address and draw them on the map
	private class NavigationTask extends AsyncTask<Address, Void, NavObject> {
		@Override
		protected NavObject doInBackground(Address... addresses) {
			NavObject navObject = new NavObject();
			Address address = addresses[0];
			navObject.setAddress(address);

			// get the lat long points from the address
			LatLng fromPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
			// set the poly line options
			rectline = new PolylineOptions().width(10).color(Color.RED);//Color.rgb(51, 181, 229));

			GMapV2Direction md = new GMapV2Direction();
			navObject.setgMapV2Direction(md);
			// get the directions
			Document doc = md.getDocument(getActivity(), fromPosition, new LatLng(address.getLatitude(), address.getLongitude()), mode);
			navObject.setDocument(doc);

			// get the directions array and loop over each point
			for (LatLng latLng : md.getDirection(doc)) {
				// add the point to the rectline
				rectline.add(latLng);
			}
			// return the address, this gets passed to onpostexecute
			return navObject;
		}

		@Override
		protected void onPostExecute(NavObject navObject) {
			Address address = navObject.getAddress();
			Document document = navObject.getDocument();
			GMapV2Direction md = navObject.getgMapV2Direction();

			TextView travel_info = (TextView) activity.findViewById(R.id.map_travel_info);

			String end_address = (address.getAddressLine(0) != null ? address.getAddressLine(0) : md.getEndAddress(document));

			travel_info.setText(md.getDurationText(document));
			travel_info.append(" (" + md.getDistanceText(document) +")");
			travel_info.append(" to " + end_address);

			// create a new latlng builder, this will keep the points displayed on the map
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			// include both current lcoation and location we're headed
			builder.include(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
			builder.include(new LatLng(address.getLatitude(), address.getLongitude()));

			// draw the poly line
			map.addPolyline(rectline);
			// animate the camera to include both points with a small buffer around them
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
			// turn on the traffic, it can make it hard to see the poly line
			map.setTrafficEnabled(false);

			// draw a marker to where we're going
			drawMarker(address);
			// also draw a marker for our starting point
			drawMarker(currentLocation);
		}
	}

	// since we need multiple objects to be passed from doinbackground to onpostexecute, it's easiest to make a custom object and add everything indo that
	private class NavObject {
		private GMapV2Direction gMapV2Direction;
		private Document document;
		private Address address;

		public void setgMapV2Direction(GMapV2Direction gMapV2Direction) {
			this.gMapV2Direction = gMapV2Direction;
		}

		public void setDocument(Document document) {
			this.document = document;
		}

		public void setAddress(Address address) {
			this.address = address;
		}

		public GMapV2Direction getgMapV2Direction() {
			return gMapV2Direction;
		}

		public Document getDocument() {
			return document;
		}

		public Address getAddress() {
			return address;
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// check the codes
		switch (requestCode) {
			case PLACE_DETAILS: {
				try {
					Address address = intent.getParcelableExtra("address");
					map.clear();
					navigating = true;
					new NavigationTask().execute(address);
				} catch (Exception e) {
					Log.e(TAG, "place details", e);
				}
			}
		}
	}

	private BroadcastReceiver mapAddressReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				// get a list of addresses that fit the search string
				ArrayList<Address> addresses = new MapHelpers.GetLocationsFromStringTask(activity, currentLocation).execute(intent.getStringExtra("formattedAddress")).get();
				// get the details of the first one
				Address address = new MapHelpers.GetDetailsFromReferenceTask(activity).execute(addresses.get(0).getUrl()).get();
				// draw a marker of it
				LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
				drawMarker(address, CameraUpdateFactory.newLatLngZoom(latLng, cameraZoom));

				showSelf = true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

		}
	};

}