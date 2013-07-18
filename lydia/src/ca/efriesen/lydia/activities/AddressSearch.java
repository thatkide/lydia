package ca.efriesen.lydia.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.includes.MapHelpers;
import ca.efriesen.lydia_common.includes.Intents;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by eric on 2013-07-12.
 */
public class AddressSearch extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		AdapterView.OnItemClickListener {

	private LocationClient locationClient;
	private Location currentLocation;
	private AddressViewAdapter adapter;
	private EditText address;
	private ListView listView;

	private static final String TAG = "lydia address search";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutInflater().inflate(R.layout.address_search, null));

		// connect to the location client
		locationClient = new LocationClient(this, this, this);
		locationClient.connect();

		listView = (ListView) findViewById(R.id.address_list);
		listView.setOnItemClickListener(this);

		address = (EditText) findViewById(R.id.address_search);
		address.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
				GetPlaces getPlaces = new GetPlaces();
				getPlaces.execute(address.getText().toString());
			}

			@Override
			public void afterTextChanged(Editable editable) { }
		});
	}

	@Override
	public void onConnected(Bundle bundle) {
		currentLocation = locationClient.getLastLocation();
	}

	@Override
	public void onDisconnected() {}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		Address address = (Address) adapterView.getAdapter().getItem(position);
		Log.d(TAG, "addess clicked " + address.getFeatureName());
		GetPlaceDetails getPlaceDetails = new GetPlaceDetails();
		getPlaceDetails.execute(address.getUrl());
	}

	private class GetPlaceDetails extends AsyncTask<String, Void, ArrayList<Address>> {
		@Override
		protected ArrayList<Address> doInBackground(String... search) {
			return MapHelpers.getDetailsFromReference(getApplicationContext(), search[0]);
		}

		@Override
		protected void onPostExecute(ArrayList<Address> result) {
			try {
				// make a new intent with the address, and send it to the map fragment
				Intent drawMarker = new Intent(Intents.DRAWMARKER);
				drawMarker.putExtra("address", result.get(0));
				sendBroadcast(drawMarker);
				// close ourself
				finish();
			} catch (Exception e) {
				Log.e(TAG, "draw marker failed", e);
			}
		}
	}

	private class GetPlaces extends AsyncTask<String, Void, ArrayList<Address>> {
		@Override
		// three dots is java for an array of strings
		protected ArrayList<Address> doInBackground(String... search) {

			Log.d("gottaGo", "doInBackground");

			return MapHelpers.getLocationsFromString(getApplicationContext(), search[0], currentLocation);
		}

		@Override
		protected void onPostExecute(ArrayList<Address> result) {
			adapter = new AddressViewAdapter(result, AddressSearch.this);
			listView.setAdapter(adapter);
		}
	}


	class AddressViewAdapter extends BaseAdapter implements ListAdapter {
		private final List<Address> content;
		private final Activity activity;

		public AddressViewAdapter(List<Address> content, Activity activity) {
			this.content = content;
			this.activity = activity;
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
			final View listEntry = inflater.inflate(android.R.layout.simple_list_item_1, null); // initialize the layout from xml
			final TextView type = (TextView) listEntry.findViewById(android.R.id.text1);
			final Address current = content.get(position);

			type.setText(current.getFeatureName());

			return listEntry;
		}
	}
}



