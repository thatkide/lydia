package com.autosenseapp.activities.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import com.autosenseapp.R;
import com.autosenseapp.activities.BaseActivity;
import com.autosenseapp.adapters.PinsAdapter;
import com.autosenseapp.adapters.TriggerAdapter;
import com.autosenseapp.controllers.PinTriggerController;
import com.autosenseapp.databases.ArduinoPin;
import com.autosenseapp.devices.actions.Action;
import com.autosenseapp.devices.outputTriggers.Trigger;
import com.ikovac.timepickerwithseconds.view.MyTimePickerDialog;
import com.ikovac.timepickerwithseconds.view.TimePicker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;

/**
 * Created by eric on 2014-08-30.
 */
public class ArduinoPinEditor extends BaseActivity implements
		View.OnClickListener,
		RadioGroup.OnCheckedChangeListener,
		MyTimePickerDialog.OnTimeSetListener {

	private static final String TAG = ArduinoPinEditor.class.getSimpleName();

	@Inject PinTriggerController pinTriggerController;

	@InjectView(R.id.pin_edit_title) TextView pinEditTitle;
	@InjectView(R.id.pin_settings_title) TextView pinSettingsTitle;
	@InjectView(R.id.arduino_action_title) TextView actionTitle;
	@InjectView(R.id.pin_comment) EditText pinComment;
	@InjectView(R.id.pin_mode) Spinner pinModes;
	@InjectView(R.id.pin_output_trigger) ListView pinTriggers;
	@InjectView(R.id.arduino_actions) RadioGroup actionGroup;
	@InjectView(R.id.arduino_pins_list) ListView pinList;
	@InjectView(R.id.action_settings_button) View actionSettings;

	private ArduinoPin selectedArduinoPin;

	private List<Action> allOutputActions;
	private List<Trigger> allOutputTriggers;
	// we need these in the action callbacks.
	private Action currentAction;
	private Trigger currentTrigger;

	@OnTextChanged(R.id.pin_comment)
	void onTextChanged(CharSequence text) {
		// get the text and save it
		selectedArduinoPin.setComment(text.toString());
		pinTriggerController.updatePin(selectedArduinoPin);
	}

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		setContentView(R.layout.arduino_pin_editor);
		// inject all the views
		ButterKnife.inject(this);
		// get these lists once
		allOutputActions = pinTriggerController.getOutputActions();
		allOutputTriggers = pinTriggerController.getOutputTriggers();

		pinTriggers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		actionGroup.setOnCheckedChangeListener(this);

		// get the map of pins that has been passed
		if (getIntent().hasExtra("pins")) {
			ArrayList<ArduinoPin> arduinoPins = getIntent().getParcelableArrayListExtra("pins");
			// get the listview and pass the map adapter
			pinList.setAdapter(new PinsAdapter(this, arduinoPins));

			pinModes.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pinTriggerController.getPinModes()));

			// Must be last
			// select the first item
			pinList.performItemClick(pinList, 0, pinList.getItemIdAtPosition(0));
		}
	}

	// Called when an item in the list of pins, or trigger is clicked
	@OnItemClick(R.id.arduino_pins_list)
	public void pinListClick(AdapterView<?> adapter, View view, int position, long id) {
		// get the pin clicked
		selectedArduinoPin = (ArduinoPin) adapter.getItemAtPosition(position);
		// update the title
		pinEditTitle.setText(getString(R.string.pin) + " " + selectedArduinoPin.toString());
		// set the text for the pin comment
		pinComment.setText(selectedArduinoPin.getComment());
		// save the selected pin.  we use this for saving and such
		pinModes.setSelection(selectedArduinoPin.getMode());
		// reset the action title
		actionTitle.setText(getString(R.string.action));
		updateTriggerList(selectedArduinoPin);
	}

	@OnItemClick(R.id.pin_output_trigger)
	public void pinTrigerClick(AdapterView<?> adapter, View view, int position, long id) {
		Trigger trigger = (Trigger) view.getTag(R.string.trigger);
		// set the action title to include the trigger
		updateActions(trigger);
	}

	// called when pin mode is selected from the spinner
	@OnItemSelected(R.id.pin_mode)
	public void onPinModeSelected(AdapterView<?> parent, View view, int position, long id) {
		// save the pin mode, input, output, high impedance...
		selectedArduinoPin.setMode(((Long) id).intValue());
		// save the selected pin mode
		pinTriggerController.updatePin(selectedArduinoPin);
		// if we selected "output", show the next spinner
		updateTriggerList(selectedArduinoPin);
	}

	// checkbox callback
	@Override
	public void onClick(View v) {
		Trigger trigger = (Trigger) v.getTag(R.string.trigger);
		// update the trigger for the selected pin
		// pass the pin, the trigger, and if we're adding (true) or removing (false)
		if (!((CheckBox) v).isChecked()) {
			pinTriggerController.removePinTrigger(selectedArduinoPin, trigger);
		}

		// update the actions.  if we are checked, pass the trigger, otherwise null.  this will remove the visual check of the radio button
		if (((CheckBox) v).isChecked()) {
			updateActions(trigger);
		} else {
			updateActions(null);
		}
	}

	@OnClick(R.id.action_settings_button)
	public void buttonClick() {
		currentAction.getExtraDialog(this, selectedArduinoPin).show();
	}

	// Radio button listener
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		RadioButton selectedRadioButton = ButterKnife.findById(group, checkedId);
		try {
			Action action = (Action) selectedRadioButton.getTag(R.string.action);
			Trigger trigger = (Trigger) selectedRadioButton.getTag(R.string.trigger);
			// get the stored action from the radio button
			if (action.hasExtra()) {
				action.getExtraDialog(this, selectedArduinoPin).show();
				// save these for the extra data callback
				currentAction = action;
				currentTrigger = trigger;
				actionSettings.setVisibility(View.VISIBLE);
			} else {
				actionSettings.setVisibility(View.INVISIBLE);
			}
			if (selectedRadioButton.isChecked()) {
				pinTriggerController.editPinTrigger(selectedArduinoPin, trigger, action);
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
		switch (selectedArduinoPin.getMode()) {
			case PinTriggerController.OUTPUT: {
				actionTitle.setVisibility(View.VISIBLE);
				pinSettingsTitle.setText(getString(R.string.trigger));
				List<Trigger> selectedTriggers = pinTriggerController.getTriggers(selectedArduinoPin);
				// pass the full list of triggers and the selected triggers for the selected pin
				pinTriggers.setAdapter(new TriggerAdapter(ArduinoPinEditor.this, allOutputTriggers, selectedTriggers));
				if (selectedTriggers.size() > 0) {
					// show the action for the top trigger
					updateActions(selectedTriggers.get(0));
				} else {
					updateActions(null);
				}
				break;
			}
			case PinTriggerController.INPUT: {
				actionTitle.setVisibility(View.GONE);
				updateActions(null);
				break;
			}
			default: {
				pinTriggers.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{}));
				updateActions(null);
			}
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
			for (Action action : allOutputActions) {
				// create a new button
				RadioButton radioButton = new RadioButton(this);
				// set the text
				radioButton.setId(action.getId());
				// if we have the needed info, show the extra data text in brackets
				if (trigger != null && trigger.getAction() != null && trigger.getAction().hasExtra() && trigger.getAction().getId() == action.getId()) {
					String extraString = (trigger.getAction().getExtraString() != null ? " (" + trigger.getAction().getExtraString() + ")" : "");
					radioButton.setText(action.getName(this) + extraString);
				} else {
					radioButton.setText(action.getName(this));
				}
				// add the action as the tag
				// The reason we use R.string.action is we need a guaranteed unique id that is precompiled.  using already defined strings is easier than making new ids
				radioButton.setTag(R.string.action, action);
				if (trigger != null && trigger.getAction() != null && trigger.getAction().getId() == action.getId()) {
					radioButton.setChecked(true);
					currentAction = trigger.getAction();
					if (currentAction.hasExtra()) {
						actionSettings.setVisibility(View.VISIBLE);
					}
				}
				if (trigger == null) {
					// disable it
					radioButton.setEnabled(false);
				} else {
					radioButton.setTag(R.string.trigger, trigger);
				}
				// add it to the view
				actionGroup.addView(radioButton);
			}
		}
	}
}
