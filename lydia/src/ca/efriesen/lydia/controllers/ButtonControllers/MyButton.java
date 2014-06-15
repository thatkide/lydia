package ca.efriesen.lydia.controllers.ButtonControllers;

import android.app.Activity;

/**
 * Created by eric on 2014-06-14.
 */
public abstract class MyButton {

	public void onClick() {	};
	public boolean onLongClick() {
		return false;
	};

	public void cleanUp() { };
}
