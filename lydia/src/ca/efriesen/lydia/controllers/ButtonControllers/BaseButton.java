package ca.efriesen.lydia.controllers.ButtonControllers;

import android.app.Activity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by eric on 2014-06-14.
 */
public abstract class BaseButton {

	public static final int TYPE_HOMESCREEN = 1;
	public static final int TYPE_SIDEBAR_LEFT = 2;
	public static final int TYPE_SIDEBAR_RIGHT = 3;

	public void onClick() {	};
	public boolean onLongClick() {
		return false;
	};

	public void cleanUp() { };
	public abstract String getAction();
	public abstract String getDescription();
	public abstract String toString();

	// create a map of all the buttons that are available
	public static Map<String, BaseButton> getButtons(Activity activity) {
		Map<String, BaseButton> buttons = new HashMap<String, BaseButton>();
		// add all the possible actions and classes
		buttons.put(AirRideButton.ACTION, new AirRideButton(activity));
		buttons.put(AndroidButton.ACTION, new AndroidButton(activity));
		buttons.put(CalendarButton.ACTION, new CalendarButton(activity));
		buttons.put(ChromeButton.ACTION, new ChromeButton(activity));
		buttons.put(ContactsButton.ACTION, new ContactsButton(activity));
		buttons.put(EngineStatusButton.ACTION, new EngineStatusButton(activity));
		buttons.put(MusicButton.ACTION, new MusicButton(activity));
		buttons.put(NavigationButton.ACTION, new NavigationButton(activity));
		buttons.put(PhoneButton.ACTION, new PhoneButton(activity));
		buttons.put(SettingsButton.ACTION, new SettingsButton(activity));
		buttons.put(VideosButton.ACTION, new VideosButton(activity));
		buttons.put(WeatherButton.ACTION, new WeatherButton(activity));

		return buttons;
	}
}
