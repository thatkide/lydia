package ca.efriesen.lydia.controllers;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import ca.efriesen.lydia.controllers.ButtonControllers.*;

import java.util.HashMap;

/**
 * Created by eric on 2014-06-14.
 */
public class ButtonController implements View.OnClickListener{

	private final static String TAG = "Lydia button controller";

	private Activity activity;

	public HashMap<String, MyButton> buttons = new HashMap<String, MyButton>();

	public ButtonController(Activity activity) {
		this.activity = activity;

		buttons.put(AirRideButton.ACTION, new AirRideButton());
		buttons.put(AndroidButton.ACTION, new AndroidButton());
		buttons.put(ChromeButton.ACTION, new ChromeButton());
		buttons.put(MusicButton.ACTION, new MusicButton());
		buttons.put(NavigationButton.ACTION, new NavigationButton());
		buttons.put(PhoneButton.ACTION, new PhoneButton());
	}

	@Override
	public void onClick(View view) {
		Bundle buttonBundle = (Bundle) view.getTag();

		MyButton button = buttons.get(buttonBundle.getString("action"));
		button.onClick(activity);
	}
}
