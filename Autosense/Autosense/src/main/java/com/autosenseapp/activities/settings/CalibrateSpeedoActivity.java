package com.autosenseapp.activities.settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.autosenseapp.R;
import com.autosenseapp.devices.IdiotLights;

/**
 * Created by eric on 2014-06-03.
 */
public class CalibrateSpeedoActivity extends Activity {

	private boolean calibrating = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.calibrate_speedo);

		registerReceiver(inputPulsesReceiver, new IntentFilter(IdiotLights.SPEEDOCALIBRATINGPULSES));

		final Button startStop = (Button) findViewById(R.id.calibrate_speedo_start_stop);
		startStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!calibrating) {
					startStop.setText(getString(R.string.calibrate_speedo_finish));
					calibrating = true;
				} else {
					startStop.setText(getString(R.string.calibrate_speedo_start));
					calibrating = false;
				}
				// create a new bundle
				Bundle data = new Bundle();
				byte values[] = {(calibrating ? (byte)1 : (byte)0)};
				data.putByte("command", (byte) IdiotLights.CALIBRATE);
				data.putByteArray("values", values);
				// send a broadcast with the data
				sendBroadcast(new Intent(IdiotLights.WRITE).putExtras(data));
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(inputPulsesReceiver);
	}

	BroadcastReceiver inputPulsesReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int pulses = intent.getIntExtra("pulses", 0);
			TextView pulseText = (TextView) findViewById(R.id.calibrate_speedo_pulses);
			pulseText.setText(String.valueOf(pulses));
		}
	};
}
