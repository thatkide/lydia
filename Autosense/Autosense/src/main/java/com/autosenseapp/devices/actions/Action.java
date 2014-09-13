package com.autosenseapp.devices.actions;

import android.app.Dialog;
import android.content.Context;
import android.os.Parcelable;

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
	public abstract Dialog getExtraDialog(Context context);
	public abstract void setExtraData(String string);
	public abstract String getExtraData();
	public abstract String getExtraString();
	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}
}
