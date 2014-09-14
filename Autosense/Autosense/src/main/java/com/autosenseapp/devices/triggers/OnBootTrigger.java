package com.autosenseapp.devices.triggers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.Parcel;
import android.util.Log;
import com.autosenseapp.GlobalClass;
import com.autosenseapp.controllers.PinTriggerController;
import com.autosenseapp.databases.ArduinoPin;
import com.autosenseapp.devices.actions.Action;
import com.autosenseapp.services.ArduinoService;

import java.util.List;

/**
 * Created by eric on 2014-09-05.
 */
public class OnBootTrigger extends Trigger {

	private static final String TAG = OnBootTrigger.class.getSimpleName();
	public static final String receiverString = OnBootTrigger.class.getSimpleName() + "Receiver";
	private boolean triggerDone = false;

	public OnBootTrigger(){}

	public OnBootTrigger(Context context) {
		super(context);
		context.registerReceiver(receiver, new IntentFilter(receiverString));
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(action, flags);
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeString(className);
	}

	public OnBootTrigger(Parcel in) {
		this.action = in.readParcelable(Action.class.getClassLoader());
		this.id = in.readInt();
		this.name = in.readString();
		this.className = in.readString();
	}

	public static final Creator CREATOR = new Creator() {
		@Override
		public Object createFromParcel(Parcel source) {
			return new OnBootTrigger(source);
		}

		@Override
		public Action[] newArray(int size) {
			return new Action[0];
		}
	};

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// only do this once
			if (triggerDone) {
				return;
			}
			// get the controller
			PinTriggerController controller = (PinTriggerController) ((GlobalClass)context.getApplicationContext()).getController(GlobalClass.PIN_TRIGGER_CONTROLLER);
			// get the list of pins that we need to deal with
			List<ArduinoPin> pins = controller.getAllTriggersByClassName(OnBootTrigger.class.getSimpleName());
			// loop over them
			int deviceType = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS).getInt(ArduinoService.ARDUINO_TYPE, ArduinoService.ARDUINO_NONE);
			Log.d(TAG, "device type " + deviceType);
			for (ArduinoPin pin : pins) {
				Log.d(TAG, "set pin " + pin.getPinNumber() + " to " + pin.getAction().getName(context));
			}
			triggerDone = true;
		}
	};
}
