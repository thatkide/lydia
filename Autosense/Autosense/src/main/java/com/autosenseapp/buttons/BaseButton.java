package com.autosenseapp.buttons;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;

import com.autosenseapp.buttons.appButtons.AirRideButton;
import com.autosenseapp.buttons.appButtons.AndroidButton;
import com.autosenseapp.buttons.appButtons.AppLaunchButton;
import com.autosenseapp.buttons.appButtons.CalendarButton;
import com.autosenseapp.buttons.appButtons.ChromeButton;
import com.autosenseapp.buttons.appButtons.ContactsButton;
import com.autosenseapp.buttons.appButtons.EngineStatusButton;
import com.autosenseapp.buttons.appButtons.MusicButton;
import com.autosenseapp.buttons.appButtons.NavigationButton;
import com.autosenseapp.buttons.appButtons.PhoneButton;
import com.autosenseapp.buttons.appButtons.SettingsButton;
import com.autosenseapp.buttons.appButtons.VideosButton;
import com.autosenseapp.buttons.appButtons.WeatherButton;
import com.autosenseapp.buttons.navButtons.ClearMapButton;
import com.autosenseapp.buttons.navButtons.MapDirectionsMode;
import com.autosenseapp.buttons.navButtons.ToggleTrafficButton;
import com.autosenseapp.buttons.settingsButtons.ArduinoSettingsButton;
import com.autosenseapp.buttons.settingsButtons.MediaSettingsButton;
import com.autosenseapp.buttons.settingsButtons.WeatherSettingsButton;
import com.autosenseapp.databases.Button;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by eric on 2014-06-14.
 */
public abstract class BaseButton extends android.widget.Button {

	public static final int GROUP_ADMIN = -1;
	public static final int GROUP_USER = 0;
	public static final int GROUP_NAVIGATION = 1;

	public static final int TYPE_ANY = 0;
	public static final int TYPE_HOMESCREEN = 1;
	public static final int TYPE_SIDEBAR_LEFT = 2;
	public static final int TYPE_SIDEBAR_RIGHT = 3;

	public static final int BUTTON_NEXT = 1;
	public static final int BUTTON_PREV = 2;
	public static final int BUTTON_SCREEN_ADD = 3;
	public static final int BUTTON_SCREEN_DELETE = 4;

	public static final int BUTTONS_PER_SIDEBAR = 3;
	public static final int BUTTONS_PER_HOMESCREEN = 6;

	private Context context;

	private String resourceName;

	public BaseButton(Context context) {
		super(context);
		this.context = context;
	}

	public abstract void onClick(View view, Button passed);
	public boolean onLongClick() {
		return false;
	};

	public void onStart() { };
	public void onStop() { };
	public final String getAction() {
		return this.getClass().getSimpleName();
	};

	public boolean hasExtraData() { return false; }
	public ArrayAdapter<?> getAdapterData() { return null; }
	// get the description from the strings.xml file
	public final String getDescription() {
		try {
			int resId = context.getResources().getIdentifier(getClass().getSimpleName() + "Desc", "string", context.getPackageName());
			return context.getString(resId);
		} catch (Exception e) {
			// No description
			return "";
		}
	};
	// get the default name from the settings.xml file
	public final String getDefaultName() {
		try {
			// In the strings.xml file the resources are specified as the class name.  get the resource and return the string
			int resid = context.getResources().getIdentifier(getClass().getSimpleName(), "string", context.getPackageName());
			return context.getString(resid);
		} catch (Exception e) {
			// No default name specified
			return "";
		}
	}

	public String getExtraData(int position) { return ""; }

	protected String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	// return the description using the tostring method.  the adapter for the spinner uses tostring
	public final String toString() {
		return getDescription();
	}

	public static Map<String, BaseButton> getAdminButtons(Activity activity) {
		Map<String, BaseButton> buttons = new HashMap<String, BaseButton>();
		// settings buttons
		buttons.put(ArduinoSettingsButton.class.getSimpleName(), new ArduinoSettingsButton(activity));
		buttons.put(MediaSettingsButton.class.getSimpleName(), new MediaSettingsButton(activity));
		buttons.put(WeatherSettingsButton.class.getSimpleName(), new WeatherSettingsButton(activity));

		// navigation buttons
		buttons.put(ClearMapButton.class.getSimpleName(), new ClearMapButton(activity));
		buttons.put(MapDirectionsMode.class.getSimpleName(), new MapDirectionsMode(activity));
		buttons.put(ToggleTrafficButton.class.getSimpleName(), new ToggleTrafficButton(activity));

		return buttons;
	}

	// create a map of all the buttons that are available
	public static Map<String, BaseButton> getUserButtons(Activity activity) {
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
		buttons.put(SettingsButton.class.getSimpleName(), new SettingsButton(activity));
		buttons.put(VideosButton.class.getSimpleName(), new VideosButton(activity));
		buttons.put(WeatherButton.class.getSimpleName(), new WeatherButton(activity));

		return buttons;
	}

	public static Map<String, BaseButton> getAllButtons(Activity activity) {
		Map<String, BaseButton> buttons = new HashMap<String, BaseButton>();
		buttons.putAll(getUserButtons(activity));
		buttons.putAll(getAdminButtons(activity));
		return buttons;
	}
}
