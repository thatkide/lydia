package com.autosenseapp.activities.settings;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import com.autosenseapp.GlobalClass;
import com.autosenseapp.R;
import com.autosenseapp.adapters.MapAdapter;
import com.autosenseapp.adapters.PinsMapAdapter;
import com.autosenseapp.adapters.TriggerAdapter;
import com.autosenseapp.controllers.PinTriggerController;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by eric on 2014-08-30.
 */
public class ArduinoPinEditor extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, View.OnClickListener {

	private static final String TAG = ArduinoPinEditor.class.getSimpleName();

	private TextView pinEditTitle;
	private TextView pinSettingsTitle;
	private TextView actionTitle;
	private TextView actionString;
	private int selectedPin;
	private ListView pinTriggers;
	private PinTriggerController pinTriggerController;
	private RadioGroup actionGroup;
	private String selectedMode;

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		setContentView(R.layout.arduino_pin_editor);

		// get the controller that manages the whole system
		pinTriggerController = (PinTriggerController) ((GlobalClass)getApplicationContext()).getController(GlobalClass.PIN_TRIGGER_CONTROLLER);

		// find the views we're interested in
		pinEditTitle = (TextView) findViewById(R.id.pin_edit_title);
		pinSettingsTitle = (TextView) findViewById(R.id.pin_settings_title);
		actionTitle = (TextView) findViewById(R.id.arduino_action_title);
		actionString = (TextView) findViewById(R.id.arduino_action_string);
		Spinner pinModes = (Spinner) findViewById(R.id.pin_mode);
		pinModes.setOnItemSelectedListener(this);
		pinModes.setAdapter(new MapAdapter(this, pinTriggerController.getPinModes()));
		pinTriggers = (ListView) findViewById(R.id.pin_output_trigger);
		pinTriggers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		pinTriggers.setOnItemClickListener(this);
		actionGroup = (RadioGroup) findViewById(R.id.arduino_actions);

		// get the map of pins that has been passed
		if (getIntent().hasExtra("pins")) {
			Map<Integer, Set<String>> pins = (HashMap<Integer, Set<String>>) getIntent().getSerializableExtra("pins");
			// get the listview and pass the map adapter
			ListView listView = (ListView) findViewById(R.id.arduino_pins_list);
			listView.setAdapter(new PinsMapAdapter(this, pins));
			listView.setOnItemClickListener(this);

			// populate available actions
			Map<String, String> actions = pinTriggerController.getActions();
			Iterator iterator = actions.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, String> action = (Map.Entry<String, String>) iterator.next();
				RadioButton button = new RadioButton(this);
				button.setText(action.getValue());
				button.setTag(action.getKey());
				actionGroup.addView(button);
			}


			// Must be last
			// select the first item
			listView.performItemClick(listView, 0, listView.getItemIdAtPosition(0));
		}
	}

	// Called when an item in the list of pins, or trigger is clicked
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		switch (adapter.getId()) {
			// Get the pin that has been clicked
			case R.id.arduino_pins_list: {
				PinsMapAdapter pinsAdapter =  ((PinsMapAdapter)adapter.getAdapter());
				// get the pin clicked
				String pinName = pinsAdapter.getName(position);
				// update the title
				pinEditTitle.setText(getString(R.string.pin) + " " + pinName);
				// save the selected pin.  we use this for saving and such
				selectedPin = pinsAdapter.getPinNumber(position);
				// set a new trigger adapter for this pin.
				pinTriggers.setAdapter(new TriggerAdapter(this, pinTriggerController.getPinTriggers()));
				break;
			}
			case R.id.pin_output_trigger: {
				TriggerAdapter triggerAdapter = (TriggerAdapter) adapter.getAdapter();
				// If the user clicked on the text of the trigger, toggle the checkbox.  It's just easier than having to click the box
				TriggerAdapter.ViewHolder viewHolder = (TriggerAdapter.ViewHolder) view.getTag();
				String currentAction = triggerAdapter.getItem(position);
				actionTitle.setText(getString(R.string.action) + ": " + currentAction);

				updateActions(viewHolder.getCheckbox());
				break;
			}
		}
		updateTextString();
	}

	// called when pin output is selected from the spinner
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		selectedMode = (String) parent.getSelectedItem();

		// if we selected "output", show the next spinner
		if (getString(R.string.output).equalsIgnoreCase(selectedMode)) {
			pinTriggers.setVisibility(View.VISIBLE);
			pinSettingsTitle.setText(getString(R.string.trigger));
		} else {
			pinTriggers.setVisibility(View.INVISIBLE);
		}
		updateTextString();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private void updateTextString() {
		actionString.setText("Set " + selectedPin + " to " + selectedMode + ".  On "  + " perform action ");
	}

	private void updateActions(CheckBox checkBox) {
		boolean isChecked = checkBox.isChecked();
		for (int i=0; i<actionGroup.getChildCount(); i++) {
			actionGroup.getChildAt(i).setEnabled(isChecked);
		}
		if (isChecked) {
			actionGroup.setVisibility(View.VISIBLE);
		}

	}

	// checkbox callback
	@Override
	public void onClick(View v) {
		updateActions((CheckBox)v);
		updateTextString();
	}
}
