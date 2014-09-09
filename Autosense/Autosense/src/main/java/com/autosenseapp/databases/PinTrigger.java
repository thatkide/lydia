package com.autosenseapp.databases;

import com.autosenseapp.devices.actions.Action;
import com.autosenseapp.devices.triggers.Trigger;

/**
 * Created by eric on 2014-09-05.
 */
public class PinTrigger {

	private int id;
	private Action action;
	private ArduinoPin arduinoPin;
	private Trigger trigger;
	private Object extraData;

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Action getAction() {
		return action;
	}

	public void setArduinoPin(ArduinoPin arduinoPin) {
		this.arduinoPin = arduinoPin;
	}

	public ArduinoPin getArduinoPin() {
		return arduinoPin;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public void setExtraData() {

	}

	public Object getExtraData() {
		return extraData;
	}

}
