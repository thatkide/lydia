package com.autosenseapp.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import com.autosenseapp.R;
import com.autosenseapp.adapters.PinsMapAdapter;
import com.autosenseapp.adapters.TriggerAdapter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by eric on 2014-08-30.
 */
public class ArduinoPinEditor extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

	private static final String TAG = ArduinoPinEditor.class.getSimpleName();

	private TextView pinEditTitle;
	private TextView pinSettingsTitle;
	private int selectedPin;
	private ListView pinTriggers;
	private List<String> triggerOptions;

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		setContentView(R.layout.arduino_pin_editor);

		pinEditTitle = (TextView) findViewById(R.id.pin_edit_title);
		pinSettingsTitle = (TextView) findViewById(R.id.pin_settings_title);
		Spinner pinModes = (Spinner) findViewById(R.id.pin_mode);
		pinModes.setOnItemSelectedListener(this);
		pinTriggers = (ListView) findViewById(R.id.pin_output_trigger);
		pinTriggers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		pinTriggers.setOnItemClickListener(this);

		// get the map of pins that has been passed
		if (getIntent().hasExtra("pins")) {
			Map<Integer, Set<String>> pins = (HashMap<Integer, Set<String>>) getIntent().getSerializableExtra("pins");
			// get the listview and pass the map adapter
			ListView listView = (ListView) findViewById(R.id.arduino_pins_list);
			listView.setAdapter(new PinsMapAdapter(this, pins));
			listView.setOnItemClickListener(this);

			// populate the options array from the string resources
			String[] triggersArray = getResources().getStringArray(R.array.pin_output_triggers);
			triggerOptions = Arrays.asList(triggersArray);

			// Must be last
			// select the first item
			listView.performItemClick(listView, 0, listView.getItemIdAtPosition(0));
		}
	}

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
				pinTriggers.setAdapter(new TriggerAdapter(this, triggerOptions));
				break;
			}
			case R.id.pin_output_trigger: {
				// If the user clicked on the text of the trigger, toggle the checkbox.  It's just easier than having to click the box
				TriggerAdapter.ViewHolder viewHolder = (TriggerAdapter.ViewHolder) view.getTag();
				viewHolder.getCheckbox().toggle();
				break;
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		String selectedMode = (String) parent.getSelectedItem();

		// if we selected "output", show the next spinner
		if (getString(R.string.output).equalsIgnoreCase(selectedMode)) {
			pinTriggers.setVisibility(View.VISIBLE);
			pinSettingsTitle.setText(getString(R.string.trigger));
		} else {
			pinTriggers.setVisibility(View.INVISIBLE);
		}

		Log.d(TAG, "pin is " + selectedPin);
		Log.d(TAG, "mode is " + parent.getSelectedItem());
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private void savePin() {

	}

}
