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
import com.autosenseapp.devices.triggers.ButtonTrigger;
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
	public void onClick(View view, Button passed) {

	}

	@Override
	public boolean hasExtraData() {
		return true;
	}

	@Override
	public ArrayAdapter<ArduinoPin> getAdapterData() {
		List<ArduinoPin> pinTriggers = pinTriggerController.getAllTriggersByClassName(ButtonTrigger.class.getSimpleName());
		adapter = new ArduinoPinAdapter(context, pinTriggers);
		return adapter;
	}


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
			viewHolder.textView.setText(pin.toString() + (pin.getComment() != null ? " - " + pin.getComment() : ""));

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
