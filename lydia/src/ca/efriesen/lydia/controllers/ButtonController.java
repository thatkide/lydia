package ca.efriesen.lydia.controllers;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import ca.efriesen.lydia.controllers.ButtonControllers.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by eric on 2014-06-14.
 */
public class ButtonController implements View.OnClickListener, View.OnLongClickListener {

	private final static String TAG = "Lydia button controller";

	// create a new hashmap that takes the action and maps it to a class
	public HashMap<String, MyButton> buttons = new HashMap<String, MyButton>();

	public ButtonController(Activity activity) {
		// add all the possible actions and classes
		buttons.put(AirRideButton.ACTION, new AirRideButton(activity));
		buttons.put(AndroidButton.ACTION, new AndroidButton(activity));
		buttons.put(ChromeButton.ACTION, new ChromeButton(activity));
		buttons.put(MusicButton.ACTION, new MusicButton(activity));
		buttons.put(NavigationButton.ACTION, new NavigationButton(activity));
		buttons.put(PhoneButton.ACTION, new PhoneButton(activity));
	}

	@Override
	public void onClick(View view) {
		// get the bundle of the button pressed
		Bundle buttonBundle = (Bundle) view.getTag();
		// get the button from the hashmap, and execute the onclick method
		MyButton button = buttons.get(buttonBundle.getString("action"));
		button.onClick();
	}

	@Override
	public boolean onLongClick(View view) {
		// get the bundle of the button pressed
		Bundle buttonBundle = (Bundle) view.getTag();
		// get the button from the hashmap, and execute the onclick method
		MyButton button = buttons.get(buttonBundle.getString("action"));
		button.onLongClick();
		return false;
	}

	public void cleanup() {
		for (Map.Entry<String , MyButton>entry : buttons.entrySet()) {
			MyButton button = entry.getValue();
			button.cleanUp();
		}
	}
}
