package com.autosenseapp.configs;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import com.autosenseapp.R;
import com.autosenseapp.buttons.settingsButtons.ArduinoSettingsButton;
import com.autosenseapp.buttons.BaseButton;
import com.autosenseapp.buttons.settingsButtons.MediaSettingsButton;
import com.autosenseapp.buttons.appButtons.SettingsButton;
import com.autosenseapp.buttons.settingsButtons.WeatherSettingsButton;
import com.autosenseapp.callbacks.ButtonCheckerCallback;
import com.autosenseapp.databases.Button;

/**
 * Created by eric on 2014-07-06.
 */
public class AdminButtonsConfig implements ButtonCheckerCallback{

	private int buttonType = BaseButton.TYPE_SIDEBAR_LEFT;
	private int group = BaseButton.GROUP_ADMIN;

	@Override
	public List<Button> getButtons(Activity activity) {
		int adminArea = 0;
		List<Button> buttons = new ArrayList<Button>();

		// create all the buttons we want in the admin panel
		Button systemSettings = new Button();
		systemSettings.setAction(SettingsButton.class.getSimpleName());
		systemSettings.setButtonType(buttonType);
		systemSettings.setDisplayArea(adminArea);
		systemSettings.setGroup(group);
		systemSettings.setPosition(0);
		systemSettings.setTitle(activity.getString(R.string.settings));
		systemSettings.setUsesDrawable(false);
		systemSettings.setDrawable("blank");

		Button mediaSettings = new Button();
		mediaSettings.setAction(MediaSettingsButton.class.getSimpleName());
		mediaSettings.setButtonType(buttonType);
		mediaSettings.setDisplayArea(adminArea);
		mediaSettings.setGroup(group);
		mediaSettings.setPosition(1);
		mediaSettings.setTitle(activity.getString(R.string.media));
		mediaSettings.setUsesDrawable(false);
		mediaSettings.setDrawable("blank");

		Button weatherSettings = new Button();
		weatherSettings.setAction(WeatherSettingsButton.class.getSimpleName());
		weatherSettings.setButtonType(buttonType);
		weatherSettings.setDisplayArea(adminArea);
		weatherSettings.setGroup(group);
		weatherSettings.setPosition(2);
		weatherSettings.setTitle(activity.getString(R.string.weather));
		weatherSettings.setUsesDrawable(false);
		weatherSettings.setDrawable("blank");

		Button arduinoSettings = new Button();
		arduinoSettings.setAction(ArduinoSettingsButton.class.getSimpleName());
		arduinoSettings.setButtonType(buttonType);
		arduinoSettings.setDisplayArea(adminArea+1);
		arduinoSettings.setGroup(group);
		arduinoSettings.setPosition(0);
		arduinoSettings.setTitle(activity.getString(R.string.arduino));
		arduinoSettings.setUsesDrawable(false);
		arduinoSettings.setDrawable("blank");

		buttons.add(systemSettings);
		buttons.add(mediaSettings);
		buttons.add(weatherSettings);
		buttons.add(arduinoSettings);

		return buttons;
	}

	@Override
	public int getGroup() {
		return group;
	}

	@Override
	public int getType() {
		return buttonType;
	}
}
