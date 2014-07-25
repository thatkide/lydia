package ca.efriesen.lydia.fragments.NotificationFragments;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia.devices.Master;
import ca.efriesen.lydia.interfaces.NotificationInterface;
import ca.efriesen.lydia_common.BluetoothService;
import ca.efriesen.lydia_common.includes.Intents;

/**
 * Created by eric on 2014-07-24.
 */
public class SystemNotificationFragment extends Fragment implements NotificationInterface {

	private Activity activity;
	private TextView outsideTemp;
	private TextView insideTemp;
	private int inside = 0;
	private int outside = 0;

	// FIXME
	// add checks if we have an arduino or not and what info to display if we don't

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.notification_system, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		activity = getActivity();
		// ensure out temperature info is updated
		byte[] data = {};
		Master.writeData(activity, Master.GETTEMP, data);
		// init the text views
		outsideTemp = (TextView) getActivity().findViewById(R.id.outside_temperature);
		insideTemp = (TextView) getActivity().findViewById(R.id.inside_temperature);
	}

	@Override
	public void onStart() {
		super.onStart();
		// register the broadcast receivers.  they get updated temp info from the arduino
		activity.registerReceiver(insideTemperatureReceiver, new IntentFilter(Master.INSIDETEMPERATURE));
		activity.registerReceiver(outsideTemperatureReceiver, new IntentFilter(Master.OUTSIDETEMPERATURE));
		activity.registerReceiver(bluetoothReceiver, new IntentFilter(Intents.BLUETOOTHMANAGER));
		activity.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	@Override
	public void onStop() {
		super.onStop();
		// unregister the receivers
		try {
			activity.unregisterReceiver(bluetoothReceiver);
		} catch (Exception e) {}
		try {
			activity.unregisterReceiver(mBatteryReceiver);
		} catch (Exception e) {}
		try {
			activity.unregisterReceiver(insideTemperatureReceiver);
		} catch (Exception e) {}
		try {
			activity.unregisterReceiver(outsideTemperatureReceiver);
		} catch (Exception e) {}
	}


	@Override
	public void saveFragment(Bundle bundle) {
		// put the temperatures into the bundle for next startup
		bundle.putInt("inside", inside);
		bundle.putInt("outside", outside);
	}

	@Override
	public void restoreFragment(Bundle bundle) {
		// ensure out temperature info is updated
		byte[] data = {};
		Master.writeData(activity, Master.GETTEMP, data);
		// use the passed info to show the temps.  anything updated will overwrite via the broadcast receiver
		insideTemp.setText(bundle.getInt("inside") + "\u2103");
		outsideTemp.setText(bundle.getInt("outside") + "\u2103");
	}

	private BroadcastReceiver insideTemperatureReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// update the local var and text view
			inside = intent.getIntExtra(Master.INSIDETEMPERATURE, 0);
			insideTemp.setText(inside + "\u2103");
		}
	};

	private BroadcastReceiver outsideTemperatureReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// update the local var and text view
			outside = intent.getIntExtra(Master.OUTSIDETEMPERATURE, 0);
			outsideTemp.setText(outside + "\u2103");
		}
	};

	private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			ImageButton btImage = (ImageButton) getActivity().findViewById(R.id.bluetooth);
			BluetoothDevice device = intent.getParcelableExtra("device");
			int state = intent.getIntExtra("state", 0);
			switch (state) {
				case BluetoothService.STATE_CONNECTED: {
					try {
						Toast.makeText(getActivity(), "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
					} catch (Exception e) {}
					btImage.setImageResource(R.drawable.device_access_bluetooth_connected);
					break;
				}
				case BluetoothService.STATE_NONE: {
					btImage.setImageResource(R.drawable.device_access_bluetooth);
					break;
				}
			}
		}
	};

	private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// get the level, and scale from the intent
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

			// use "float" to do the math, since it will be decimals.  convert back to integer by *100, this adds a ".0" to the end
			float batteryPct = level / (float) scale * 100;
			// find the text view
			TextView battery = (TextView) activity.findViewById(R.id.battery_pct);
			// get the value of in string form, and update the view
			battery.setText(String.valueOf((int)batteryPct) + "%");

			// add some color if the battery is low
			if ((int)batteryPct < 10) {
				battery.setTextColor(Color.RED);
			} else if ((int)batteryPct < 25) {
				battery.setTextColor(Color.YELLOW);
			} else {
				battery.setTextColor(Color.WHITE);
			}
		}
	};

}
