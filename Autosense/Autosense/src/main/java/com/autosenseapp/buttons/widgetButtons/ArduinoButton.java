package com.autosenseapp.buttons.widgetButtons;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.autosenseapp.AutosenseApplication;
import com.autosenseapp.buttons.BaseButton;
import com.autosenseapp.controllers.PinTriggerController;
import com.autosenseapp.databases.ArduinoPin;
import com.autosenseapp.databases.Button;
import com.autosenseapp.devices.outputTriggers.ButtonTrigger;
import java.util.List;
import javax.inject.Inject;
import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by eric on 2014-09-17.
 */
public class ArduinoButton extends BaseButton {

	@Inject	PinTriggerController pinTriggerController;
	private ArrayAdapter<ArduinoPin> adapter;
	private Context context;

	public ArduinoButton(Context context) {
		super(context);
		this.context = context;
		((AutosenseApplication)context.getApplicationContext()).inject(this);
	}

	@Override
	// here we actually do our magic
	public void onClick(View view, Button passed) {
		pinTriggerController.doAction(view, Integer.parseInt(passed.getExtraData()));
	}

	@Override
	// return true to signal we want the secondary spinner
	public boolean hasExtraData() {
		return true;
	}

	@Override
	// the editor will ask what our data we want saved based on the passed item
	public String getExtraData(int position) {
		if (adapter.getCount() > 0) {
			ArduinoPin pin = adapter.getItem(position);
			return String.valueOf(pin.getPinTriggerId());
		} else {
			return "";
		}
	}

	@Override
	// we need to pass an adapter back for the spinner to be populated
	public ArrayAdapter<ArduinoPin> getAdapterData() {
		List<ArduinoPin> pinTriggers = pinTriggerController.getAllTriggersByClassName(ButtonTrigger.class.getSimpleName());
		adapter = new ArduinoPinAdapter(context, pinTriggers);
		return adapter;
	}

	// custom array adapter for the spinner display
	private class ArduinoPinAdapter extends ArrayAdapter<ArduinoPin> {

		public ArduinoPinAdapter(Context context, List<ArduinoPin> pins) {
			super(context, android.R.layout.simple_spinner_dropdown_item, pins);
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			return getView(position, convertView, parent);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;

			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
				viewHolder = new ViewHolder(convertView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			ArduinoPin pin = getItem(position);
			viewHolder.textView.setText(pin.toString() + (!pin.getComment().equalsIgnoreCase("") ? " - " + pin.getComment() : ""));

			return convertView;
		}
	}

	static class ViewHolder {
		@InjectView(android.R.id.text1) TextView textView;
		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}
}
