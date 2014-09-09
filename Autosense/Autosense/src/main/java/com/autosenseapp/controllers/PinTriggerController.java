package com.autosenseapp.controllers;

import android.app.Activity;

import com.autosenseapp.R;
import com.autosenseapp.databases.ArduinoPinsDataSource;
import com.autosenseapp.databases.ArduinoPin;
import com.autosenseapp.databases.PinTriggersDataSource;
import com.autosenseapp.devices.actions.Action;
import com.autosenseapp.devices.triggers.Trigger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by eric on 2014-09-02.
 */
public class PinTriggerController extends Controller {

	private static final String TAG = PinTriggerController.class.getSimpleName();

	public static final int HIGH_IMPEDANCE = 0;
	public static final int INPUT= 1;
	public static final int OUTPUT = 2;

	private final ArduinoPinsDataSource arduinoPinsDataSource;
	private final PinTriggersDataSource pinTriggersDataSource;
	private List<String> pinModes;

	public PinTriggerController(Activity activity) {
		super(activity);

		arduinoPinsDataSource = new ArduinoPinsDataSource(activity);
		pinTriggersDataSource = new PinTriggersDataSource(activity);

		pinModes = new ArrayList<String>();
		pinModes.add(HIGH_IMPEDANCE, activity.getString(R.string.high_impedance));
		pinModes.add(INPUT, activity.getString(R.string.input));
		pinModes.add(OUTPUT, activity.getString(R.string.output));
	}

	@Override
	public void onDestroy() {
		arduinoPinsDataSource.close();
		pinTriggersDataSource.close();
	}

	public String getDescription(int pin) {
		return "";
//		return ("Set " + selectedPin + " to " + selectedMode + ".  On "  + " perform action ");
	}

	public List<String> getPinModes() {
		return Collections.unmodifiableList(pinModes);
	}

	public List<Action> getActions() {
		return Collections.unmodifiableList(arduinoPinsDataSource.getActions());
	}

	public List<Trigger> getTriggers() {
		return Collections.unmodifiableList(arduinoPinsDataSource.getTriggers());
	}

	public void updatePin(ArduinoPin arduinoPin) {
		arduinoPinsDataSource.updatePin(arduinoPin);
	}

	public void addPinTriggers(ArduinoPin arduinoPin, Trigger trigger, Action action) {
		pinTriggersDataSource.addPinTrigger(arduinoPin, trigger, action);
	}

	public void editPinTrigger(ArduinoPin arduinoPin, Trigger trigger, Action action) {
		pinTriggersDataSource.editPinTrigger(arduinoPin, trigger, action);
	}

	public void removePinTrigger(ArduinoPin arduinoPin, Trigger trigger) {
		pinTriggersDataSource.removePinTrigger(arduinoPin, trigger);
	}
}
