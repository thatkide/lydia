package com.autosenseapp.databases;

import android.os.Parcel;
import android.os.Parcelable;

import com.autosenseapp.devices.actions.Action;
import com.autosenseapp.devices.triggers.Trigger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric on 2014-09-05.
 */
public class ArduinoPin implements Parcelable {

	public static final int ANALOG = 0;
	public static final int DIGITAL = 1;

	private int id;
	private int pinNumber;
	private String name;
	private int mode;
	private int pinType;
	private List<Trigger> triggerList;
	private Action action;
	private String comment;

	public ArduinoPin() { }

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setPinNumber(int pinNumber) {
		this.pinNumber = pinNumber;
	}

	public int getPinNumber() {
		return pinNumber;
	}

	public void setPinName(String name) {
		if (name != null && name.length() > 2) {
			this.name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		} else {
			this.name = "";
		}
	}

	public String getPinName() {
		return name;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getMode() {
		return mode;
	}

	public void setPinType(int pinType) {
		this.pinType = pinType;
	}

	public int getPinType() {
		return pinType;
	}

	public void setTriggers(List<Trigger> triggerList) {
		this.triggerList = triggerList;
	}

	public List<Trigger> getTriggers() {
		return triggerList;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Action getAction() {
		return action;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	@Override
	public String toString() {
		return pinNumber + (name == null || name.isEmpty() ? "" : " (" + name + ")");
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public ArduinoPin(Parcel in) {
		this.id = in.readInt();
		this.pinNumber = in.readInt();
		this.name = in.readString();
		this.mode = in.readInt();
		this.pinType = in.readInt();
		this.comment = in.readString();
		this.action = in.readParcelable(Action.class.getClassLoader());
		triggerList = new ArrayList<Trigger>();
		in.readList(triggerList, Trigger.class.getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(pinNumber);
		dest.writeString(name);
		dest.writeInt(mode);
		dest.writeInt(pinType);
		dest.writeString(comment);
		dest.writeParcelable(action, flags);
		dest.writeList(triggerList);
	}

	public static final Creator CREATOR = new Creator() {
		@Override
		public Object createFromParcel(Parcel source) {
			return new ArduinoPin(source);
		}

		@Override
		public Object[] newArray(int size) {
			return new ArduinoPin[size];
		}
	};
}
