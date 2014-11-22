package com.autosenseapp.devices.configs;

import android.app.Activity;
import android.content.Intent;
import com.autosenseapp.AutosenseApplication;
import com.autosenseapp.activities.settings.ArduinoPinEditor;
import com.autosenseapp.controllers.PinTriggerController;
import com.autosenseapp.databases.ArduinoPin;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by eric on 2014-08-30.
 */
public class ArduinoDue implements ArduinoConfig {

	private static final String TAG = ArduinoDue.class.getSimpleName();

	@Inject PinTriggerController pinTriggerController;

	private Activity activity;

	private ArrayList<ArduinoPin> greenList;
	private ArrayList<ArduinoPin> redList;
	private ArrayList<ArduinoPin> blueList;
	private ArrayList<ArduinoPin> pinkList;
	private ArrayList<ArduinoPin> yellowList;
	private ArrayList<ArduinoPin> orangeList;

	public ArduinoDue(Activity activity) {
		this.activity = activity;
		// add ourself to the dependency injector list
		((AutosenseApplication)activity.getApplication()).inject(this);
		getPins();
	}

	@Override
	public void handleClick(int color) {
		Intent intent = new Intent(activity, ArduinoPinEditor.class);
		switch (color) {
			// Reset
			case BLACK: {
				break;
			}
			// 0 - 7
			case GREEN: {
				intent.putParcelableArrayListExtra("pins", greenList);
				break;
			}
			// 8 - 13, I2C 1
			case RED: {
				intent.putParcelableArrayListExtra("pins", redList);
				break;
			}
			// Communication - 14 - 21
			case BLUE: {
				intent.putParcelableArrayListExtra("pins", blueList);
				break;
			}
			// 22 - 53
			case PINK: {
				intent.putParcelableArrayListExtra("pins", pinkList);
				break;
			}
			// A0 - A7
			case YELLOW: {
				intent.putParcelableArrayListExtra("pins", yellowList);
				break;
			}
			// A8 - A11, DAC0, DAC1, CAN
			case ORANGE: {
				intent.putParcelableArrayListExtra("pins", orangeList);
				break;
			}
			default: {
				// no nothing
				return;
			}
		}

		activity.startActivity(intent);
	}

	@Override
	public void onResume() {
		getPins();
	}

	private void getPins() {
		// get both digital and analog pins
		List<ArduinoPin> digitalPins = pinTriggerController.getPins(ArduinoPin.DIGITAL);
		List<ArduinoPin> analogPins = pinTriggerController.getPins(ArduinoPin.ANALOG);

		// split the pins into their respective color lists
		greenList = new ArrayList<ArduinoPin>(digitalPins.subList(0, 8));	// 0-7
		redList = new ArrayList<ArduinoPin>(digitalPins.subList(8, 14));	// 8-13
		blueList = new ArrayList<ArduinoPin>(digitalPins.subList(14, 22));	// 14-21
		pinkList = new ArrayList<ArduinoPin>(digitalPins.subList(22, 55));	// 21-54
		yellowList = new ArrayList<ArduinoPin>(analogPins.subList(0, 8));	// A0-A7
		orangeList = new ArrayList<ArduinoPin>(analogPins.subList(8, 12));	// A8-A11
	}
}
