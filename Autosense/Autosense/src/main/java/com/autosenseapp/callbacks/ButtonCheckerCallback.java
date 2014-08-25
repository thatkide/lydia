package com.autosenseapp.callbacks;

import android.app.Activity;

import java.util.List;

import com.autosenseapp.databases.Button;

/**
 * Created by eric on 2014-07-06.
 */
public interface ButtonCheckerCallback {
	public List<Button> getButtons(Activity activity);
	public int getGroup();
	public int getType();
}
