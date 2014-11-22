package com.autosenseapp.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.autosenseapp.R;
import com.autosenseapp.devices.IdiotLights;

/**
 * User: eric
 * Date: 2013-01-05
 * Time: 10:51 PM
 */
public class EngineStatusFragment extends Fragment {

	private static final String TAG = "lydia engine status fragment";

	private TextView fuelView;
	private TextView rpmView;
	private TextView speedView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.engine_status_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		try {
			getActivity().registerReceiver(idiotLightsReceiver, new IntentFilter(IdiotLights.IDIOTLIGHTS));
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		fuelView = (TextView) getActivity().findViewById(R.id.fuelView);
		rpmView = (TextView) getActivity().findViewById(R.id.rpmView);
		speedView = (TextView) getActivity().findViewById(R.id.speedView);
	}

	@Override
	public void onStop() {
		super.onStop();

		try {
			getActivity().unregisterReceiver(idiotLightsReceiver);
		} catch (Exception e) {}
	}

	private BroadcastReceiver idiotLightsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra(IdiotLights.CURRENTFUEL)) {
				fuelView.setText(intent.getStringExtra(IdiotLights.CURRENTFUEL));
			}
			if (intent.hasExtra(IdiotLights.CURRENTRPM)) {
				rpmView.setText(intent.getStringExtra(IdiotLights.CURRENTRPM));
			}
			if (intent.hasExtra(IdiotLights.CURRENTSPEED)) {
				speedView.setText(intent.getStringExtra(IdiotLights.CURRENTSPEED));
			}
		}
	};
}