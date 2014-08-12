package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.Dashboard;
import ca.efriesen.lydia.activities.PlaceDetails;
import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.buttons.navButtons.ShowHide;
import ca.efriesen.lydia.callbacks.FragmentAnimationCallback;
import ca.efriesen.lydia.callbacks.FragmentOnBackPressedCallback;
import ca.efriesen.lydia.includes.MapHelpers;
import ca.efriesen.lydia_common.includes.Constants;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * User: eric
 * Date: 2013-05-18
 * Time: 4:17 PM
 */
public class MapFragment extends Fragment implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		GoogleMap.OnMapClickListener,
		GoogleMap.OnMapLongClickListener,
		LocationSource.OnLocationChangedListener,
		GoogleMap.OnCameraChangeListener,
		GoogleMap.OnMyLocationButtonClickListener,
		FragmentAnimationCallback, FragmentOnBackPressedCallback, RoutingListener {

	private static final String TAG = MapFragment.class.getSimpleName();

	public static final int PLACE_DETAILS = 1;
	public static final int ADDRESS_SEARCH = 2;
	public static final int NAV_MODE = 3;

	private Activity activity;
	private GoogleMap map;
	private MapView mapView;

	private int mapType = GoogleMap.MAP_TYPE_NORMAL;
	private boolean showTraffic = true;
	private boolean followLocation = true;
	private float cameraZoom = 16;
	private Routing.TravelMode travelMode;

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private LocationClient locationClient;
	private Location currentLocation;
	private Address address;

	private EditText search;

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		// create a new location client
		locationClient = new LocationClient(getActivity(), this, this);
		locationClient.connect();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.map_fragment, container, false);
		// Gets the MapView from the XML layout and creates it

		try {
			MapsInitializer.initialize(getActivity());
		} catch (Exception e) {
			Log.e("Address Map", "Could not initialize google play", e);
		}

		switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity())) {
			case ConnectionResult.SUCCESS: {
				mapView = (MapView) v.findViewById(R.id.map);
				mapView.onCreate(savedInstanceState);
				// Gets to GoogleMap from the MapView and does initialization stuff
				if (mapView != null) {
					map = mapView.getMap();

					if (map != null) {
						map.setMapType(mapType);
						map.setTrafficEnabled(showTraffic);
						map.setMyLocationEnabled(true);
						map.setOnMapClickListener(this);
						map.setOnMapLongClickListener(this);
						map.setOnMyLocationButtonClickListener(this);

						UiSettings settings = map.getUiSettings();
						settings.setAllGesturesEnabled(true);
						settings.setCompassEnabled(true);
						settings.setIndoorLevelPickerEnabled(true);
						settings.setMyLocationButtonEnabled(true);
						settings.setRotateGesturesEnabled(true);
						settings.setScrollGesturesEnabled(true);
						settings.setTiltGesturesEnabled(true);
						settings.setZoomControlsEnabled(true);
						settings.setZoomGesturesEnabled(true);
					}
				}
				break;
			}
		}
		return v;
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		activity = getActivity();

		// disable gesture listener.  otherwise it takes over the map controls
		((Dashboard)activity).getGestureOverlayView().removeAllOnGesturePerformedListeners();

		int mode = activity.getSharedPreferences(activity.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS).getInt("nav_mode", 1);
		travelMode = Routing.TravelMode.values()[mode];

		final ShowHide showHide = new ShowHide(activity);
		final Button button = (Button) activity.findViewById(R.id.hide_driver_controls);
		final ListView listView = (ListView) activity.findViewById(R.id.search_results);
		search = (EditText) activity.findViewById(R.id.search_string);


		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showHide.onClick(v, button);
			}
		});

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// this is just a skeleton address object.  we'll get the details below
				Address address = (Address) parent.getAdapter().getItem(position);

				try {
					address = new MapHelpers.GetDetailsFromReferenceTask(activity).execute(address.getUrl()).get();
					// close keyboard
					InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						activity.getWindow().getDecorView().setSystemUiVisibility(
								View.SYSTEM_UI_FLAG_LAYOUT_STABLE
										| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
										| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
										| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
						);
					}
					// clear any markers
					map.clear();
					// update the camera
					CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), 16);
					// animate it
					map.animateCamera(cameraUpdate);
					// draw new marker
					drawMarker(address, true);
					// clear the search text
					search.setText("");
					// clear the adapter
					((AddressViewAdapter)parent.getAdapter()).clear();
					// hide the results
					listView.setVisibility(View.INVISIBLE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (Exception e) {
					Log.e(TAG, "draw marker failed", e);
				}
			}
		});

		search.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
				try {
					ArrayList<Address> addresses = new MapHelpers.GetLocationsFromStringTask(activity, currentLocation).execute(search.getText().toString()).get();
					AddressViewAdapter adapter = new AddressViewAdapter(addresses, activity);
					listView.setAdapter(adapter);
					listView.setVisibility(View.VISIBLE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void afterTextChanged(Editable editable) { }
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
		locationClient.disconnect();
	}

	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// reenable gesture listener
		((Dashboard)activity).getGestureOverlayView().addOnGesturePerformedListener((Dashboard)activity);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}

	@Override
	public void onConnected(Bundle data) {
		// when the location provider is connected
		// set the current location to the last known location
		currentLocation = locationClient.getLastLocation();
		if (map != null && currentLocation != null) {
			// move the camera to the current location
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 10);
			map.animateCamera(cameraUpdate);
		}
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

	public void drawMarker(Location location, boolean showWindow) {
		try {
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			Address address =  new MapHelpers.GetAddressFromLatLngTask().execute(latLng).get();
			// draw the marker
			drawMarker(address, showWindow);
			// move the camera to that spot
//			map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, cameraZoom));
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public void drawMarker(Address address, CameraUpdate cameraUpdate, boolean showWindow) {
		drawMarker(address, showWindow);
		map.animateCamera(cameraUpdate);
	}

	// method that draws a marker based on an address
	public void drawMarker(final Address address, boolean showWindow) {
		try {
			Marker marker = map.addMarker(new MarkerOptions()
					.position(new LatLng(address.getLatitude(), address.getLongitude()))
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
					.title(address.getFeatureName()));
			if (showWindow) {
				marker.showInfoWindow();
			}
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
//		// create a new marker and set the position to the position that was clicked
//		Marker marker = map.addMarker(new MarkerOptions()
//				.position(new LatLng(latLng.latitude, latLng.longitude))
//				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
//				.title(getString(R.string.loading_address)));
//		// show the window
//		marker.showInfoWindow();
//
//		// get the address from the latlng pressed
//		// this is an async task and will replace the loading marker drawn above
//		try {
//			Address address =  new MapHelpers.GetAddressFromLatLngTask().execute(latLng).get();
//			// draw the marker
//			drawMarker(address);
//			// move the camera to that spot
////			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), cameraZoom));
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void onLocationChanged(Location location) {
		currentLocation = location;
		if (followLocation) {
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), cameraZoom);
			map.animateCamera(cameraUpdate);
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// check the codes
		switch (requestCode) {
			case PLACE_DETAILS: {
				if (resultCode == Activity.RESULT_OK) {
					try {
						Address address = intent.getParcelableExtra("address");
						map.clear();
						Routing routing = new Routing(travelMode);
						routing.registerListener(this);
						routing.execute(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), new LatLng(address.getLatitude(), address.getLongitude()));
					} catch (Exception e) { }
				}
				break;
			}
			case NAV_MODE: {
				if (resultCode == Activity.RESULT_OK) {
					travelMode = (Routing.TravelMode) intent.getSerializableExtra("mode");

					// Reload driver controls with nav buttons
					DriverControlsFragment driverControlsFragment = new DriverControlsFragment();
					Bundle args = new Bundle();
					args.putInt("group", BaseButton.GROUP_NAVIGATION);
					args.putInt("selectedDriverBar", 1);
					driverControlsFragment.setArguments(args);

					activity.getFragmentManager().beginTransaction()
							.replace(R.id.driver_controls, driverControlsFragment)
							.addToBackStack(null)
							.commit();
				}
				break;
			}
		}
	}


	@Override
	public void onBackPressed() {
		((PassengerControlsFragment) activity.getFragmentManager().findFragmentById(R.id.passenger_controls)).showFragment(this);
		((DriverControlsFragment) activity.getFragmentManager().findFragmentById(R.id.driver_controls)).showFragment(null);
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

	@Override
	public void animationStart(int direction) { }

	public boolean toggleTraffic() {
		showTraffic = !showTraffic;
		map.setTrafficEnabled(showTraffic);
		return showTraffic;
	}

	public void clearMap() {
		map.clear();
	}

	@Override
	public void onRoutingFailure() {
	}

	@Override
	public void onRoutingStart() {
	}

	@Override
	public void onRoutingSuccess(PolylineOptions mPolyOptions) {
		PolylineOptions polylineOptions = new PolylineOptions();
		polylineOptions.color(Constants.FilterColor);

		polylineOptions.width(10);
		polylineOptions.addAll(mPolyOptions.getPoints());
		map.addPolyline(polylineOptions);

		// include a starting marker
		drawMarker(currentLocation, false);
		// and a finish marker
		drawMarker(address, true);

		// set zoom to include all points if possible
		List<LatLng> points = mPolyOptions.getPoints();
		LatLngBounds.Builder builder = new LatLngBounds.Builder();

		for (LatLng item : points) {
			builder.include(item);
		}

		map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
	}

	@Override
	public boolean onMyLocationButtonClick() {
		if (!locationClient.isConnected()) {
			locationClient.connect();
		}
		Toast.makeText(getActivity(), "Waiting for location...", Toast.LENGTH_SHORT).show();
		return false;
	}


	class AddressViewAdapter extends BaseAdapter implements ListAdapter {
		private final List<Address> content;
		private final Activity activity;

		public AddressViewAdapter(List<Address> content, Activity activity) {
			this.content = content;
			this.activity = activity;
		}

		public void clear() {
			content.clear();
		}

		public int getCount() {
			return content.size();
		}

		public Address getItem(int position) {
			return content.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView,	ViewGroup parent) {
			final LayoutInflater inflater = activity.getLayoutInflater();   // default layout inflater
			final View listEntry = inflater.inflate(android.R.layout.simple_list_item_1, parent, false); // initialize the layout from xml
			final TextView type = (TextView) listEntry.findViewById(android.R.id.text1);
			type.setTextColor(Color.BLACK);
			final Address current = content.get(position);

			type.setText(current.getFeatureName());

			return listEntry;
		}
	}

}