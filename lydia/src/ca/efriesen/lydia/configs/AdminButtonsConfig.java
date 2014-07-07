package ca.efriesen.lydia.configs;

import java.util.ArrayList;
import java.util.List;

import ca.efriesen.lydia.buttons.settingsButtons.ArduinoSettingsButton;
import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.buttons.settingsButtons.MediaSettingsButton;
import ca.efriesen.lydia.buttons.appButtons.SettingsButton;
import ca.efriesen.lydia.buttons.settingsButtons.WeatherSettingsButton;
import ca.efriesen.lydia.callbacks.ButtonCheckerCallback;
import ca.efriesen.lydia.databases.Button;

/**
 * Created by eric on 2014-07-06.
 */
public class AdminButtonsConfig implements ButtonCheckerCallback{

	private int buttonType = BaseButton.TYPE_SIDEBAR_LEFT;
	private int group = BaseButton.GROUP_ADMIN;

	@Override
	public List<Button> getButtons() {
		int adminArea = 0;
		List<Button> buttons = new ArrayList<Button>();

		// create all the buttons we want in the admin panel
		Button systemSettings = new Button();
		systemSettings.setAction(SettingsButton.class.getSimpleName());
		systemSettings.setButtonType(buttonType);
		systemSettings.setDisplayArea(adminArea);
		systemSettings.setGroup(group);
		systemSettings.setPosition(0);
		systemSettings.setTitle("Settings");
		systemSettings.setUsesDrawable(false);
		systemSettings.setDrawable("blank");

		Button mediaSettings = new Button();
		mediaSettings.setAction(MediaSettingsButton.class.getSimpleName());
		mediaSettings.setButtonType(buttonType);
		mediaSettings.setDisplayArea(adminArea);
		mediaSettings.setGroup(group);
		mediaSettings.setPosition(1);
		mediaSettings.setTitle("Media");
		mediaSettings.setUsesDrawable(false);
		mediaSettings.setDrawable("blank");

		Button weatherSettings = new Button();
		weatherSettings.setAction(WeatherSettingsButton.class.getSimpleName());
		weatherSettings.setButtonType(buttonType);
		weatherSettings.setDisplayArea(adminArea);
		weatherSettings.setGroup(group);
		weatherSettings.setPosition(2);
		weatherSettings.setTitle("Weather");
		weatherSettings.setUsesDrawable(false);
		weatherSettings.setDrawable("blank");

		Button arduinoSettings = new Button();
		arduinoSettings.setAction(ArduinoSettingsButton.class.getSimpleName());
		arduinoSettings.setButtonType(buttonType);
		arduinoSettings.setDisplayArea(adminArea+1);
		arduinoSettings.setGroup(group);
		arduinoSettings.setPosition(0);
		arduinoSettings.setTitle("Arduino");
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
