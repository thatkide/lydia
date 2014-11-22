package com.autosenseapp.devices.outputTriggers;

import android.content.Context;
import android.os.Parcel;
import com.autosenseapp.devices.actions.Action;

/**
 * Created by eric on 2014-09-05.
 */
public class ButtonTrigger extends Trigger {

	public ButtonTrigger() {
		super();
	}

	public ButtonTrigger(Context context) {
		super(context);
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

	public ButtonTrigger(Parcel in) {
		this.action = in.readParcelable(Action.class.getClassLoader());
		this.id = in.readInt();
		this.name = in.readString();
		this.className = in.readString();
	}

	public static final Creator CREATOR = new Creator() {
		@Override
		public Object createFromParcel(Parcel source) {
			return new ButtonTrigger(source);
		}

		@Override
		public Action[] newArray(int size) {
			return new Action[0];
		}
	};

}
