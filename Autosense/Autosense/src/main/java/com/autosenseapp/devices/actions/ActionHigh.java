package com.autosenseapp.devices.actions;

import android.app.Dialog;
import android.content.Context;
import android.os.Parcel;
import com.autosenseapp.R;

/**
 * Created by eric on 2014-09-04.
 */
public class ActionHigh extends Action {

	public ActionHigh() {
		super();
	}

	@Override
	public String getName(Context context) {
		return context.getString(R.string.low);
	}

	@Override
	public boolean hasExtra() {
		return false;
	}

	@Override
	public Dialog getExtraDialog(Context context) {
		return null;
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
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(name);
	}

	public ActionHigh(Parcel in) {
		this.id = in.readInt();
		this.name = in.readString();
	}

	public static final Creator CREATOR = new Creator() {
		@Override
		public Object createFromParcel(Parcel source) {
			return new ActionHigh(source);
		}

		@Override
		public Action[] newArray(int size) {
			return new Action[0];
		}
	};
}
