package com.autosenseapp.activities.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import com.autosenseapp.GlobalClass;
import com.autosenseapp.R;
import com.autosenseapp.adapters.TriggerAdapter;
import com.autosenseapp.controllers.PinTriggerController;
import com.autosenseapp.databases.ArduinoPin;
import com.autosenseapp.databases.Button;
import com.autosenseapp.devices.actions.Action;
import com.autosenseapp.devices.triggers.Trigger;
import com.ikovac.timepickerwithseconds.view.MyTimePickerDialog;
import com.ikovac.timepickerwithseconds.view.TimePicker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by eric on 2014-08-30.
 */
public class ArduinoPinEditor extends Activity implements
		AdapterView.OnItemClickListener,
		AdapterView.OnItemSelectedListener,
		View.OnClickListener,
		RadioGroup.OnCheckedChangeListener,
		MyTimePickerDialog.OnTimeSetListener {

	private static final String TAG = ArduinoPinEditor.class.getSimpleName();

	private PinTriggerController pinTriggerController;

	private TextView pinEditTitle;
	private TextView pinSettingsTitle;
	private TextView actionTitle;

	private ArduinoPin selectedArduinoPin;
	private Spinner pinModes;
	private ListView pinTriggers;

	private RadioGroup actionGroup;

	private List<Action> allActions;
	private List<Trigger> allTriggers;
	// we need these in the action callbacks.
	private Action currentAction;
	private Trigger currentTrigger;


	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		setContentView(R.layout.arduino_pin_editor);

		// get the controller that manages the whole system
		pinTriggerController = (PinTriggerController) ((GlobalClass)getApplicationContext()).getController(GlobalClass.PIN_TRIGGER_CONTROLLER);

		// get these lists once
		allActions = pinTriggerController.getActions();
		allTriggers = pinTriggerController.getTriggers();

		// find the views we're interested in
		pinEditTitle = (TextView) findViewById(R.id.pin_edit_title);
		pinSettingsTitle = (TextView) findViewById(R.id.pin_settings_title);
		actionTitle = (TextView) findViewById(R.id.arduino_action_title);

		pinModes = (Spinner) findViewById(R.id.pin_mode);
		pinModes.setOnItemSelectedListener(this);

		pinTriggers = (ListView) findViewById(R.id.pin_output_trigger);
		pinTriggers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		pinTriggers.setOnItemClickListener(this);

		actionGroup = (RadioGroup) findViewById(R.id.arduino_actions);
		actionGroup.setOnCheckedChangeListener(this);

		// get the map of pins that has been passed
		if (getIntent().hasExtra("pins")) {
			ArrayList<ArduinoPin> arduinoPins = getIntent().getParcelableArrayListExtra("pins");
			// get the listview and pass the map adapter
			ListView pinList = (ListView) findViewById(R.id.arduino_pins_list);
			pinList.setAdapter(new ArrayAdapter<ArduinoPin>(this, android.R.layout.simple_list_item_1, arduinoPins));
			pinList.setOnItemClickListener(this);

			pinModes.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pinTriggerController.getPinModes()));

			// Must be last
			// select the first item
			pinList.performItemClick(pinList, 0, pinList.getItemIdAtPosition(0));
		}
	}

	// Called when an item in the list of pins, or trigger is clicked
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		switch (adapter.getId()) {
			// Get the pin that has been clicked
			case R.id.arduino_pins_list: {
				// get the pin clicked
				selectedArduinoPin = (ArduinoPin) adapter.getItemAtPosition(position);
				// update the title
				pinEditTitle.setText(getString(R.string.pin) + " " + selectedArduinoPin.toString());
				// save the selected pin.  we use this for saving and such
				pinModes.setSelection(selectedArduinoPin.getMode());
				// reset the action title
				actionTitle.setText(getString(R.string.action));
				updateTriggerList(selectedArduinoPin);
				break;
			}
			// if the trigger has been changed
			case R.id.pin_output_trigger: {
				Trigger trigger = (Trigger) view.getTag(R.string.trigger);
				// set the action title to include the trigger
				updateActions(trigger);
				break;
			}
		}
	}

	// called when pin mode is selected from the spinner
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// save the pin mode, input, output, high impedance...
		selectedArduinoPin.setMode(((Long) id).intValue());
		// save the selected pin mode
		pinTriggerController.updatePin(selectedArduinoPin);
		// if we selected "output", show the next spinner

		updateTriggerList(selectedArduinoPin);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {	}

	// checkbox callback
	@Override
	public void onClick(View v) {
		Log.d(TAG, "clicked " + ((CheckBox)v).isChecked());
		Trigger trigger = (Trigger) v.getTag(R.string.trigger);
		// update the trigger for the selected pin
		// pass the pin, the trigger, and if we're adding (true) or removing (false)
		if (!((CheckBox) v).isChecked()) {
			Log.d(TAG, "remove trigger");
			pinTriggerController.removePinTrigger(selectedArduinoPin, trigger);
		}

//		Log.d(TAG, trigger.getAction().getName(this) + " " + trigger.getAction().getId());
		// update the actions.  if we are checked, pass the trigger, otherwise null.  this will remove the visual check of the radio button
		if (((CheckBox) v).isChecked()) {
			Log.d(TAG, "update action with trigger");
			Log.d(TAG, trigger.getName(this));
			updateActions(trigger);
		} else {
			Log.d(TAG, "update action, no trigger");
			updateActions(null);
		}
	}

	// Radio button listener
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		Log.d(TAG, "on check changed");
		RadioButton selectedRadioButton = (RadioButton) group.findViewById(checkedId);
		try {
			Action action = (Action) selectedRadioButton.getTag(R.string.action);
			Log.d(TAG, "checked id " + checkedId);
			Log.d(TAG, "aciton id " + action.getId());
			Trigger trigger = (Trigger) selectedRadioButton.getTag(R.string.trigger);
			// get the stored action from the radio button
			if (action.hasExtra()) {
				action.getExtraDialog(this).show();
				// save these for the extra data callback
				currentAction = action;
				currentTrigger = trigger;
			}
			if (selectedRadioButton.isChecked()) {
				Log.d(TAG, "it's checked, save it");
				pinTriggerController.addPinTriggers(selectedArduinoPin, trigger, action);
			}
		} catch (NullPointerException e) {}
	}

	@Override
	public void onTimeSet(TimePicker view, int hours, int minutes, int seconds) {
		int totalTime = 0;
		totalTime += TimeUnit.HOURS.toSeconds(hours);
		totalTime += TimeUnit.MINUTES.toSeconds(minutes);
		totalTime += seconds;

		currentAction.setExtraData(String.valueOf(totalTime));
		pinTriggerController.editPinTrigger(selectedArduinoPin, currentTrigger, currentAction);
	}

	private void updateTriggerList(ArduinoPin selectedArduinoPin) {
		if (selectedArduinoPin.getMode() == PinTriggerController.OUTPUT) {
			pinSettingsTitle.setText(getString(R.string.trigger));
			List<Trigger> selectedTriggers = pinTriggerController.getTriggers(selectedArduinoPin);
			// pass the full list of triggers and the selected triggers for the selected pin
			pinTriggers.setAdapter(new TriggerAdapter(ArduinoPinEditor.this, allTriggers, selectedTriggers));
			if (selectedTriggers.size() > 0) {
				// show the action for the top trigger
				updateActions(selectedTriggers.get(0));
			} else {
				updateActions(null);
			}
		} else {
			pinTriggers.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{}));
			updateActions(null);
		}
	}

	private void updateActions(Trigger trigger) {
		if (trigger != null) {
			actionTitle.setText(getString(R.string.action) + ": " + trigger.getName(this));
		} else {
			actionTitle.setText(getString(R.string.action));
		}
		actionGroup.removeAllViews();
		if (selectedArduinoPin.getMode() == PinTriggerController.OUTPUT) {
			// populate available actions
			// Loop over all the radio buttons
			for (Action action : allActions) {
				// create a new button
				RadioButton button = new RadioButton(this);
				// set the text
				button.setId(action.getId());
				if (trigger != null && trigger.getAction() != null && trigger.getAction().hasExtra() && trigger.getAction().getId() == action.getId()) {
					button.setText(action.getName(this) + " (" + trigger.getAction().getExtraString() + ")");
				} else {
					button.setText(action.getName(this));
				}
				// add the action as the tag
				// The reason we use R.string.action is we need a guaranteed unique id that is precompiled.  using already defined strings is easier than making new ids
				button.setTag(R.string.action, action);
				if (trigger != null && trigger.getAction() != null && trigger.getAction().getId() == action.getId()) {
					button.setChecked(true);
				}
				if (trigger == null) {
					// disable it
					button.setEnabled(false);
				} else {
					button.setTag(R.string.trigger, trigger);
				}
				// add it to the view
				actionGroup.addView(button);
			}
		}
	}
}
