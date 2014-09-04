package com.autosenseapp.controllers;

import android.app.Activity;
import android.util.Log;
import com.autosenseapp.R;
import com.autosenseapp.databases.PinTriggersDataSource;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by eric on 2014-09-02.
 */
public class PinTriggerController extends Controller {

	private static final String TAG = PinTriggerController.class.getSimpleName();

	private Map<String, String> actions;
	private Map<String, String> deviceTypes;
	private Map<String, String> pinModes;
	private Map<String, String> pinTriggers;
	private final PinTriggersDataSource pinTriggersDataSource;

	public PinTriggerController(Activity activity) {
		super(activity);

		pinTriggersDataSource = new PinTriggersDataSource(activity);

		// populate available actions for pin triggers
		actions = new TreeMap<String, String>();
		actions.put("on", activity.getString(R.string.on));
		actions.put("off", activity.getString(R.string.off));
		actions.put("toggle", activity.getString(R.string.toggle));
		actions.put("timer", activity.getString(R.string.timer));

		// device types. should only be two, accessory and device
		deviceTypes = new TreeMap<String, String>();
		deviceTypes.put("accessory", activity.getString(R.string.accessory));
		deviceTypes.put("device", activity.getString(R.string.device));

		// pin modes. input, output, and high impedance
		pinModes = new TreeMap<String, String>();
		pinModes.put("input", activity.getString(R.string.input));
		pinModes.put("output", activity.getString(R.string.output));
		pinModes.put("high_impedance", activity.getString(R.string.high_impedance));

		// pin triggers.  when to activate the action configured
		pinTriggers = new TreeMap<String, String>();
		pinTriggers.put("button_only", activity.getString(R.string.button_only));
		pinTriggers.put("on_boot", activity.getString(R.string.on_boot));
	}

	@Override
	public void onDestroy() {
		pinTriggersDataSource.close();
	}

	public Map<String, String> getActions() {
		return Collections.unmodifiableMap(actions);
	}

	public Map<String, String> getDeviceTypes() {
		return Collections.unmodifiableMap(deviceTypes);
	}

	public Map<String, String> getPinModes() {
		return Collections.unmodifiableMap(pinModes);
	}

	public Map<String, String> getPinTriggers() {
		return Collections.unmodifiableMap(pinTriggers);
	}
}
