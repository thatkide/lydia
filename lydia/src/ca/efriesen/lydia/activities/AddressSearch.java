package ca.efriesen.lydia.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

	private static final String TAG = "lydia map address search";

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
				try {
					ArrayList<Address> addresses = new MapHelpers.GetLocationsFromStringTask(getApplicationContext(), currentLocation).execute(address.getText().toString()).get();
					adapter = new AddressViewAdapter(addresses, AddressSearch.this);
					listView.setAdapter(adapter);
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

		try {
			ArrayList<Address> addresses = new MapHelpers.GetDetailsFromReferenceTask(getApplicationContext()).execute(address.getUrl()).get();
			// make a new intent with the address, and send it to the map fragment
			Intent addressIntent = new Intent();
			addressIntent.putExtra("address", addresses.get(0));
			setResult(RESULT_OK, addressIntent);
			// close ourself
			finish();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(TAG, "draw marker failed", e);
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



