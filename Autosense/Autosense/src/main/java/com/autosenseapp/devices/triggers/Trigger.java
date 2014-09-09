package com.autosenseapp.devices.triggers;

import android.content.Context;
import android.os.Parcelable;
import com.autosenseapp.devices.actions.Action;

/**
 * Created by eric on 2014-09-05.
 */
public abstract class Trigger implements Parcelable{

	private static final String TAG = Trigger.class.getSimpleName();

	protected int id;
	protected Action action;
	protected String name;

	public Trigger() {}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public abstract String getName(Context context);

	public void setAction(Action action) {
		this.action = action;
	}

	public Action getAction() {
		return action;
	}

	@Override
	public String toString() {
		return name;
	}
}
