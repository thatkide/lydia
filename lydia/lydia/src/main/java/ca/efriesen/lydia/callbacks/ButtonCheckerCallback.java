package ca.efriesen.lydia.callbacks;

import android.app.Activity;

import java.util.List;

import ca.efriesen.lydia.databases.Button;

/**
 * Created by eric on 2014-07-06.
 */
public interface ButtonCheckerCallback {
	public List<Button> getButtons(Activity activity);
	public int getGroup();
	public int getType();
}
