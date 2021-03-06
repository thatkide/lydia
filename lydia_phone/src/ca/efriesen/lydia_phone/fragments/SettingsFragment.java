package ca.efriesen.lydia_phone.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ca.efriesen.lydia_common.BluetoothService;
import ca.efriesen.lydia_common.includes.Intents;
import ca.efriesen.lydia_phone.R;

/**
 * User: eric
 * Date: 2013-06-08
 * Time: 10:18 AM
 */
public class SettingsFragment extends Fragment {
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.settings_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		getActivity().registerReceiver(bluetoothStatusReceiver, new IntentFilter(Intents.BLUETOOTHMANAGER));
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().sendBroadcast(new Intent(Intents.BLUETOOTHGETSTATE));
	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			getActivity().unregisterReceiver(bluetoothStatusReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver bluetoothStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			TextView status = (TextView) getActivity().findViewById(R.id.bluetooth_status);
			int state = intent.getIntExtra("state", 0);
			switch (state) {
				case BluetoothService.STATE_CONNECTED: {
					status.setText("Connected");
					break;
				}
				case BluetoothService.STATE_NONE: {
					status.setText("Disconnected");
					break;
				}
			}
		}
	};

}
