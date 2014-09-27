package com.autosenseapp.devices.outputTriggers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcel;

import com.autosenseapp.controllers.PinTriggerController;
import com.autosenseapp.databases.ArduinoPin;
import com.autosenseapp.devices.actions.Action;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by eric on 2014-09-05.
 */
public class OnBootTrigger extends Trigger {

	private static final String TAG = OnBootTrigger.class.getSimpleName();
	public static final String receiverString = OnBootTrigger.class.getSimpleName() + "Receiver";
	private boolean triggerDone = false;
	@Inject PinTriggerController pinTriggerController;

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
			// get the list of pins that we need to deal with
			List<ArduinoPin> pinTriggers = pinTriggerController.getAllTriggersByClassName(ButtonTrigger.class.getSimpleName());
			for (ArduinoPin pin : pinTriggers) {
				pinTriggerController.doAction(pin.getPinTriggerId());
			}
			triggerDone = true;
		}
	};
}
