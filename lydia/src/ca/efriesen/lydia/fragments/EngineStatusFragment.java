package ca.efriesen.lydia.fragments;

import android.app.Activity;
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
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.devices.MJLJ;
import ca.efriesen.lydia_common.includes.Intents;

import java.lang.Override;

/**
 * User: eric
 * Date: 2013-01-05
 * Time: 10:51 PM
 */
public class EngineStatusFragment extends Fragment {

	private static final String TAG = "lydia engine status fragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.engine_status_fragment, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onstart");
		try {
			getActivity().registerReceiver(mjljReceiver, new IntentFilter(Intents.MJLJ));
		} catch (Exception e) {}
	}

	@Override
	public void onStop() {
		super.onStop();

		try {
			getActivity().unregisterReceiver(mjljReceiver);
		} catch (Exception e) {}
	}

	private BroadcastReceiver mjljReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			MJLJ mjlj = (MJLJ) intent.getSerializableExtra(Intents.MJLJ);

			Activity activity = getActivity();
			TextView advance = (TextView) activity.findViewById(R.id.engine_advance);
			TextView load = (TextView) activity.findViewById(R.id.engine_load);
			TextView rpm = (TextView) activity.findViewById(R.id.engine_rpm);

			advance.setText("Advance: " + mjlj.getAdvance());
			load.setText("Load: " + mjlj.getLoad());
			rpm.setText("RPM: " + mjlj.getRpm());
		}
	};
}