package com.autosenseapp.devices.actions;

import android.app.Dialog;
import android.content.Context;
import android.os.Parcelable;
import android.view.View;
import android.widget.LinearLayout;

import com.autosenseapp.databases.ArduinoPin;
import com.autosenseapp.devices.Master;

/**
 * Created by eric on 2014-09-04.
 */
public abstract class Action implements Parcelable {

	protected int id;
	protected String name;
	protected String className;

	public Action() {}

	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName(Context context) {
		return context.getResources().getString((context.getResources().getIdentifier(name, "string", context.getPackageName())));
	}
	public abstract boolean hasExtra();
	public abstract Dialog getExtraDialog(Context context, ArduinoPin arduinoPin);
	public abstract void setExtraData(String string);
	public abstract String getExtraData();
	public abstract String getExtraString();
	public void setClassName(String className) {
		this.className = className;
	}
	public String getClassName() {
		return className;
	}
	public abstract void setView(Context context, View view, ArduinoPin pin);

	public abstract void doAction(Context context, ArduinoPin pin);
	protected void doAction(Context context, ArduinoPin pin, int command) {
		byte[] data = {(byte)pin.getPinNumber()};
		Master.writeData(context, command, data);
	}
}
