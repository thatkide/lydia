package com.autosenseapp.controllers;

import android.app.Activity;

/**
 * Created by eric on 2014-08-05.
 */
public abstract class Controller {

	protected Activity activity;
	public Controller(Activity activity) {
		this.activity = activity;
	}

	public abstract void onDestroy();
}
