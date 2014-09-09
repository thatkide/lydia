package com.autosenseapp.devices.actions;

import android.app.Dialog;
import android.content.Context;
import android.os.Parcel;

import com.autosenseapp.R;
import com.ikovac.timepickerwithseconds.view.MyTimePickerDialog;
import com.ikovac.timepickerwithseconds.view.TimePicker;

/**
 * Created by eric on 2014-09-04.
 */
public class ActionTimer extends Action {

	private String extraData;

	public ActionTimer() {
		super();
	}

	@Override
	public String getName(Context context) {
		return context.getString(R.string.timer);
	}

	@Override
	public boolean hasExtra() {
		return true;
	}

	@Override
	public Dialog getExtraDialog(final Context context) {
		return new MyTimePickerDialog(context, new MyTimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute, int seconds) {
				if (context instanceof MyTimePickerDialog.OnTimeSetListener) {
					((MyTimePickerDialog.OnTimeSetListener) context).onTimeSet(view, hourOfDay, minute, seconds);
				}
			}
		}, 0, 0, 0);
	}

	@Override
	public void setExtraData(String extraData) {
		this.extraData = extraData;
	}

	@Override
	public String getExtraData() {
		return extraData;
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

	public ActionTimer(Parcel in) {
		this.id = in.readInt();
		this.name = in.readString();
	}

	public static final Creator CREATOR = new Creator() {
		@Override
		public Object createFromParcel(Parcel source) {
			return new ActionTimer(source);
		}

		@Override
		public Action[] newArray(int size) {
			return new Action[0];
		}
	};

}
