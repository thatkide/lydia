package ca.efriesen.lydia.buttons;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import ca.efriesen.lydia.databases.Button;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by eric on 2014-06-14.
 */
public abstract class BaseButton {

	public static final int TYPE_HOMESCREEN = 1;
	public static final int TYPE_SIDEBAR_LEFT = 2;
	public static final int TYPE_SIDEBAR_RIGHT = 3;

	public static final int BUTTON_NEXT = 1;
	public static final int BUTTON_PREV = 2;
	public static final int BUTTON_SCREEN_ADD = 3;
	public static final int BUTTON_SCREEN_DELETE = 4;

	private Activity activity;

	public BaseButton(Activity activity) {
		this.activity = activity;
	}

	public abstract void onClick(View view, Button passed);
	public boolean onLongClick() {
		return false;
	};

	public void cleanUp() { };
	public final String getAction() {
		return this.getClass().getSimpleName();
	};

	public boolean hasExtraData() { return false; }
	public ArrayAdapter<?> getAdapterData() { return null; }
	// get the description from the strings.xml file
	public final String getDescription() {
		try {
			int resId = activity.getResources().getIdentifier(getClass().getSimpleName() + "Desc", "string", activity.getPackageName());
			return activity.getString(resId);
		} catch (Exception e) {
			// No description
			return "";
		}
	};
	// get the default name from the settings.xml file
	public final String getDefaultName() {
		try {
			// In the strings.xml file the resources are specified as the class name.  get the resource and return the string
			int resid = activity.getResources().getIdentifier(getClass().getSimpleName(), "string", activity.getPackageName());
			return activity.getString(resid);
		} catch (Exception e) {
			// No default name specified
			return "";
		}
	}

	public String getExtraData(int position) { return ""; }

	// return the description using the tostring method.  the adapter for the spinner uses tostring
	public final String toString() {
		return getDescription();
	}

	// create a map of all the buttons that are available
	public static Map<String, BaseButton> getButtons(Activity activity) {
		Map<String, BaseButton> buttons = new HashMap<String, BaseButton>();
		// add all the possible actions and classes
		buttons.put(AirRideButton.class.getSimpleName(), new AirRideButton(activity));
		buttons.put(AndroidButton.class.getSimpleName(), new AndroidButton(activity));
		buttons.put(AppLaunchButton.class.getSimpleName(), new AppLaunchButton(activity));
		buttons.put(CalendarButton.class.getSimpleName(), new CalendarButton(activity));
		buttons.put(ChromeButton.class.getSimpleName(), new ChromeButton(activity));
		buttons.put(ContactsButton.class.getSimpleName(), new ContactsButton(activity));
		buttons.put(EngineStatusButton.class.getSimpleName(), new EngineStatusButton(activity));
		buttons.put(MusicButton.class.getSimpleName(), new MusicButton(activity));
		buttons.put(NavigationButton.class.getSimpleName(), new NavigationButton(activity));
		buttons.put(PhoneButton.class.getSimpleName(), new PhoneButton(activity));
		buttons.put(SeatHeatButton.class.getSimpleName(), new SeatHeatButton(activity));
		buttons.put(SettingsButton.class.getSimpleName(), new SettingsButton(activity));
		buttons.put(VideosButton.class.getSimpleName(), new VideosButton(activity));
		buttons.put(WeatherButton.class.getSimpleName(), new WeatherButton(activity));

		return buttons;
	}
}
