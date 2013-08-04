package ca.efriesen.lydia.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.BluetoothDeviceDataSource;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class DeviceListActivity extends Activity implements OnClickListener{
	// Debugging
	private static final String TAG = "lydia Device List Activity";
	private static final boolean D = true;

	private BluetoothDeviceDataSource dataSource;

	// Member fields
	private BluetoothAdapter mBtAdapter;
	private ArrayList<BluetoothDevice> pairedDevices = new ArrayList<BluetoothDevice>();
	private ArrayList<BluetoothDevice> newDevices = new ArrayList<BluetoothDevice>();
	private BluetoothDeviceViewAdapter mPairedDevicesArrayAdapter;
	private BluetoothDeviceViewAdapter mNewDevicesArrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);

		// Set result CANCELED in case the user backs out
		setResult(Activity.RESULT_CANCELED);

		// Initialize the button to perform device discovery
		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doDiscovery();
				v.setEnabled(false);
			}
		});

		// Get the local Bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		pairedDevices.addAll(mBtAdapter.getBondedDevices());

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mPairedDevicesArrayAdapter = new BluetoothDeviceViewAdapter(pairedDevices, this, true);
		mNewDevicesArrayAdapter = new BluetoothDeviceViewAdapter(newDevices, this, false);

		// Find and set up the ListView for paired devices
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);

		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemLongClickListener(mDeviceLongClickListener);

		// Find and set up the ListView for newly discovered devices
		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		this.registerReceiver(bluetoothBondStateReceiver, filter);

		// show the title for paired devices, if we have any
		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			findViewById(R.id.title_paired_devices_hr).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		dataSource = new BluetoothDeviceDataSource(this);
		dataSource.open();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Make sure we're not doing discovery anymore
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}

		// Unregister broadcast listeners
		this.unregisterReceiver(mReceiver);
		this.unregisterReceiver(bluetoothBondStateReceiver);
		dataSource.close();
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		if (D)
			Log.d(TAG, "doDiscovery()");

		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);

		// Turn on sub-title for new devices
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
		findViewById(R.id.title_new_devices_hr).setVisibility(View.VISIBLE);

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBtAdapter.startDiscovery();
	}

	private AdapterView.OnItemLongClickListener mDeviceLongClickListener = new AdapterView.OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
			BluetoothDevice device = (BluetoothDevice) adapterView.getAdapter().getItem(position);

			Toast.makeText(getApplicationContext(), getString(R.string.unpairing_device) + " \"" + device.getName() + "\"", Toast.LENGTH_SHORT).show();
			unpairDevice(device);

			pairedDevices.clear();
			pairedDevices.addAll(mBtAdapter.getBondedDevices());

			mPairedDevicesArrayAdapter.notifyDataSetChanged();
			return false;
		}
	};

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			// Cancel discovery because it's costly and we're about to connect
			mBtAdapter.cancelDiscovery();

			BluetoothDevice device = (BluetoothDevice) adapterView.getAdapter().getItem(position);
			pairDevice(device);
		}
	};

	private void pairDevice(BluetoothDevice device) {
		Toast.makeText(getApplicationContext(), getString(R.string.pairing_device) + ": " + device.getName(), Toast.LENGTH_SHORT).show();
		try {
			Method m = device.getClass().getMethod("createBond", (Class[]) null);
			m.invoke(device, (Object[]) null);

		} catch (Exception e) {
			Log.e("pairDevice()", e.getMessage());
		}
	}

	private void unpairDevice(BluetoothDevice device) {
		try {
			Method m = device.getClass().getMethod("removeBond", (Class[]) null);
			m.invoke(device, (Object[]) null);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		// ensure it's no longer in the db
		dataSource.removeDevice(device);
	}

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					newDevices.add(device);
					mNewDevicesArrayAdapter.notifyDataSetChanged();
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.select_device);
				if (newDevices.size() == 0) {
					Button scanButton = (Button) findViewById(R.id.button_scan);
					scanButton.setEnabled(true);
				}
			}
		}
	};

	private final BroadcastReceiver bluetoothBondStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			BluetoothDevice device = extras.getParcelable(BluetoothDevice.EXTRA_DEVICE);

			switch (extras.getInt(BluetoothDevice.EXTRA_BOND_STATE)) {
				case BluetoothDevice.BOND_BONDED: {
					Toast.makeText(getApplicationContext(), device.getName() + " " + getString(R.string.paired), Toast.LENGTH_SHORT).show();
					pairedDevices.clear();
					pairedDevices.addAll(mBtAdapter.getBondedDevices());
					mPairedDevicesArrayAdapter.notifyDataSetChanged();

					newDevices.remove(device);
					mNewDevicesArrayAdapter.notifyDataSetChanged();
					break;
				}
			}
		}
	};

	@Override
	public void onClick(View view) {
		CheckBox checkBox = (CheckBox) view;
		BluetoothDevice device = pairedDevices.get((Integer)view.getTag());
		Log.d(TAG, device.getName());
		if (checkBox.isChecked()) {
			dataSource.addDevice(device);
		} else {
			dataSource.removeDevice(device);
		}
	}

	// listview adapter for appinfos
	class BluetoothDeviceViewAdapter extends BaseAdapter {
		private final List<BluetoothDevice> content;
		private final Activity activity;
		private final boolean showCheckbox;

		public BluetoothDeviceViewAdapter(List<BluetoothDevice> content, Activity activity, boolean showCheckbox) {
			this.content = content;
			this.activity = activity;
			this.showCheckbox = showCheckbox;
		}

		public int getCount() {
			return content.size();
		}

		public BluetoothDevice getItem(int position) {
			return content.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView,	ViewGroup parent) {
			// inflate the view if not already done
			if (convertView == null) {
				LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.device_list_row, null);
			}

			// get the specific app we've pressed
			BluetoothDevice device = content.get(position);
			if (device != null) {
				// get the name view
				TextView name = (TextView) convertView.findViewById(R.id.bluetooth_device_name);

				if (!showCheckbox) {
					convertView.findViewById(R.id.bluetooth_device_use_in_app).setVisibility(View.GONE);
					convertView.findViewById(R.id.bluetooth_device_use_in_app_text).setVisibility(View.GONE);
				}
				convertView.findViewById(R.id.bluetooth_device_use_in_app).setOnClickListener(DeviceListActivity.this);
				convertView.findViewById(R.id.bluetooth_device_use_in_app).setTag(position);
				((CheckBox)convertView.findViewById(R.id.bluetooth_device_use_in_app)).setChecked(dataSource.isDeviceInDB(device));

				// set the name
				name.setText(device.getName());
			}
			// return the view
			return convertView;
		}
	}

}
