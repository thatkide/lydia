package com.autosenseapp.devices.triggers;

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

	@Override
	public String getName(Context context) {
		int resId = context.getResources().getIdentifier(name, "string", context.getPackageName());
		return context.getString(resId);
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
	}

	public ButtonTrigger(Parcel in) {
		this.action = in.readParcelable(Action.class.getClassLoader());
		this.id = in.readInt();
		this.name = in.readString();
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
