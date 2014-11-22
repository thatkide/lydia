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
public class ArduinoUno implements ArduinoConfig {

	private static final String TAG = ArduinoDue.class.getSimpleName();

	@Inject
	PinTriggerController pinTriggerController;

	private Activity activity;

	private ArrayList<ArduinoPin> greenList;
	private ArrayList<ArduinoPin> redList;
	private ArrayList<ArduinoPin> yellowList;

	public ArduinoUno(Activity activity) {
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
				intent.putExtra("pins", greenList);
				break;
			}
			// 8 - 13
			case RED: {
				intent.putExtra("pins", redList);
				break;
			}
			// A0 - A5
			case YELLOW: {
				intent.putExtra("pins", yellowList);
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
		List<ArduinoPin> digitalPins = pinTriggerController.getPins(ArduinoPin.DIGITAL);
		List<ArduinoPin> analogPins = pinTriggerController.getPins(ArduinoPin.ANALOG);

		// new map for "green zone"
		greenList = new ArrayList<ArduinoPin>(digitalPins.subList(0, 8));	// 0-7
		redList = new ArrayList<ArduinoPin>(digitalPins.subList(8, 14));	// 8-13
		yellowList = new ArrayList<ArduinoPin>(analogPins.subList(0, 6));	// A0-A5
	}
}
