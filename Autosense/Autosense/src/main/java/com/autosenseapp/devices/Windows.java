package com.autosenseapp.devices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.MotionEvent;
import android.view.View;

import com.autosenseapp.services.ArduinoService;
import ca.efriesen.lydia_common.includes.Intents;
//import com.hoho.android.usbserial.util.SerialInputOutputManager;


/**
 * Created by eric on 2013-05-28.
 */
public class Windows extends Device {
	private static final String TAG = "windows";

	// timers for the ui control buttons
	private static int buttonDebounceTime = 500;
	private static Long buttonDownTime, buttonUpTime;

	private Context context;
//	private SerialInputOutputManager serialInputOutputManager = null;

	public Windows(Context context, String intentFilter) {
		super(context);
		this.context = context;

		context.registerReceiver(windowsReceiver, new IntentFilter(intentFilter));
	}

	@Override
	public void cleanUp() {
		try {
			context.unregisterReceiver(windowsReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setListener(ArduinoService.ArduinoListener listener) {

	}

	@Override
	public void parseData(int sender, int length, int[] data, int checksum) {

	}

	private BroadcastReceiver windowsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
//			if (intent.getIntExtra("button", 0) == R.id.driver_window_up && !intent.getBooleanExtra("stop", false)) {
//				// write the bytes to the arduino
//				write(ByteBuffer.allocate(4).putInt(Constants.DWINDOWUP).array());
//				Log.d(TAG, "Driver up");
//			} else if (intent.getIntExtra("button", 0) == R.id.driver_window_up && intent.getBooleanExtra("stop", false)) {
//				// write the bytes to the arduino
//				write(ByteBuffer.allocate(4).putInt(Constants.DWINDOWSTOP).array());
//				Log.d(TAG, "Driver up stop");
//				// send driver window up stop to arduino
//			} else if (intent.getIntExtra("button", 0) == R.id.driver_window_down && !intent.getBooleanExtra("stop", false)) {
//				// write the bytes to the arduino
//				write(ByteBuffer.allocate(4).putInt(Constants.DWINDOWDOWN).array());
//				Log.d(TAG, "Driver down");
//				// send driver window down to arduino
//			} else if (intent.getIntExtra("button", 0) == R.id.driver_window_down && intent.getBooleanExtra("stop", false)) {
//				// write the bytes to the arduino
//				write(ByteBuffer.allocate(4).putInt(Constants.DWINDOWSTOP).array());
//				Log.d(TAG, "Driver down stop");
//				// send driver window down stop
//			} else if (intent.getIntExtra("button", 0) == R.id.passenger_window_up && !intent.getBooleanExtra("stop", false)) {
//				// write the bytes to the arduino
//				write(ByteBuffer.allocate(4).putInt(Constants.PWINDOWUP).array());
//				Log.d(TAG, "Passenger up");
//				// send passenger window up
//			} else if (intent.getIntExtra("button", 0) == R.id.passenger_window_up && intent.getBooleanExtra("stop", false)) {
//				// write the bytes to the arduino
//				write(ByteBuffer.allocate(4).putInt(Constants.PWINDOWSTOP).array());
//				Log.d(TAG, "Passenger up stop");
//				// send passenger window up stop
//			} else if (intent.getIntExtra("button", 0) == R.id.passenger_window_down && !intent.getBooleanExtra("stop", false)) {
//				// write the bytes to the arduino
//				write(ByteBuffer.allocate(4).putInt(Constants.PWINDOWDOWN).array());
//				Log.d(TAG, "Passenger down");
//				// send passenger window down
//			} else if (intent.getIntExtra("button", 0) == R.id.passenger_window_down && intent.getBooleanExtra("stop", false)) {
//				// write the bytes to the arduino
//				write(ByteBuffer.allocate(4).putInt(Constants.PWINDOWSTOP).array());
//				Log.d(TAG, "Passenger down stop");
//				// send passenger window down stop
//			}
		}
	};

	public void write(byte[] command) {
		try {
			// write the bytes to the arduino
//			serialInputOutputManager.writeAsync(command);
		} catch (NullPointerException e) {}
	}

	public static Intent sendWindowCommand(View view, MotionEvent motionEvent) {
		// prepare a new intent for the hardware manager
		Intent windowControl = new Intent(Intents.WINDOWCONTROL);
		windowControl.putExtra("button", view.getId());
		// window up
		if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
			buttonDownTime = System.currentTimeMillis();
			windowControl.putExtra("stop", false);
		} else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
			buttonUpTime = System.currentTimeMillis();
			// if the button is pressed for less than the debouce time, roll all the way up, otherwise stop when released
			if (buttonUpTime - buttonDownTime > buttonDebounceTime) {
				windowControl.putExtra("stop", true);
				//	windowStop();
			}
		}
		return windowControl;
	}

}
