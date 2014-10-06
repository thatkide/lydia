package com.autosenseapp.devices.actions;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.autosenseapp.databases.ArduinoPin;
import com.autosenseapp.devices.Arduino;
import com.autosenseapp.devices.Master;
import com.autosenseapp.dialogs.ActionToggleExtraDialog;

/**
 * Created by eric on 2014-09-04.
 */
public class ActionToggle extends Action {

	private static final String TAG = ActionToggle.class.getSimpleName();

	public static final String PREFHIGH = "pref_toggle_high";
	public static final String PREFLOW = "pref_toggle_low";

	public ActionToggle() {
		super();
	}

	@Override
	public boolean hasExtra() {
		return true;
	}

	@Override
	public Dialog getExtraDialog(Context context, ArduinoPin arduinoPin) {
		return new ActionToggleExtraDialog(context, arduinoPin);
	}

	@Override
	public void setExtraData(String string) {
	}

	@Override
	public String getExtraData() {
		return null;
	}

	@Override
	public String getExtraString() {
		return null;
	}

	@Override
	public void setView(Context context, final View view, final ArduinoPin arduinoPin) {
		// First get our colors for this pin
		SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		final int highColor = sharedPreferences.getInt(PREFHIGH + arduinoPin.getId(), Color.WHITE);
		final int lowColor = sharedPreferences.getInt(PREFLOW + arduinoPin.getId(), Color.WHITE);

		// This is kinda messed up, but here's how it works
		// Because we send data to the Arduino and receive it back asynchronously we need to wait...
		// register a receiver for the pin change broadcast
		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, final Intent intent) {
				// Tell the receiver to goasync. it will wait for upto 10 seconds
				final PendingResult result = goAsync();
				// new async task
				new AsyncTask<Void, Void, Void>() {
					// we dont' need anything in the background... just do nothing
					@Override
					protected Void doInBackground(Void... params) { return null; }

					// here is the good part
					@Override
					protected void onPostExecute(Void r) {
						// get the pin from the received intent
						int pin = intent.getIntExtra("pin", 0);
						// compare it to the pin received from the method call
						if (arduinoPin.getPinNumber() == pin) {
							// get the state
							int state = intent.getIntExtra("state", 0);
							// if it's 1 (high)
							if (state == 1) {
								// set the color
								((Button)view).setTextColor(highColor);
							} else {
								((Button)view).setTextColor(lowColor);
							}
						}
						// tell the receiver we're done
						result.finish();
					}
				}.execute();
				// unregister the receiver
				context.unregisterReceiver(this);
			}
		};
		// initial broadcast register
		context.registerReceiver(receiver, new IntentFilter(Arduino.PIN_STATE));
	}

	@Override
	public void doAction(Context context, ArduinoPin pin) {
		super.doAction(context, pin, Master.TOGGLE);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeString(className);
	}

	public ActionToggle(Parcel in) {
		this.id = in.readInt();
		this.name = in.readString();
		this.className = in.readString();
	}

	public static final Creator CREATOR = new Creator() {
		@Override
		public Object createFromParcel(Parcel source) {
			return new ActionToggle(source);
		}

		@Override
		public Action[] newArray(int size) {
			return new Action[0];
		}
	};

}
