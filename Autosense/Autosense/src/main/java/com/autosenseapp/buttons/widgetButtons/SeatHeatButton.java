package com.autosenseapp.buttons.widgetButtons;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.List;

import com.autosenseapp.buttons.BaseButton;
import com.autosenseapp.databases.Button;
import com.autosenseapp.devices.Master;

/**
 * Created by eric on 2014-07-01.
 */
public class SeatHeatButton extends BaseButton {

	private static final String TAG = "seat heater";

	private Activity activity;
	private SharedPreferences sharedPreferences;

	private final byte DRIVER_SEAT = 0;
	private final byte PASSNEGER_SEAT = 1;

	private List<String> seats = new ArrayList<String>();

	private final byte LEVEL_0 = 0;
	private final byte LEVEL_1 = 1;
	private final byte LEVEL_2 = 2;

	private byte currentLevel;
	private byte seat;

	private ArrayAdapter<String> adapter;

	public SeatHeatButton(Activity activity) {
		super(activity);
		this.activity = activity;
		sharedPreferences = activity.getSharedPreferences(activity.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);

		currentLevel = (byte) sharedPreferences.getInt("seat" + seat + "level", 0);

		// add seats to the list
		seats.add(DRIVER_SEAT, "Driver Seat");
		seats.add(PASSNEGER_SEAT, "Passenger Seat");
	}

	@Override
	public void onClick(View view, Button passed) {
		seat = Byte.valueOf(passed.getExtraData());
		// move to the next heat level
		changeLevel(view);
		byte data[] = {seat, currentLevel};
		Master.writeData(activity, Master.SEATHEAT, data);
	}

	@Override
	public ArrayAdapter<String> getAdapterData() {
		adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_dropdown_item, seats);
		return adapter;
	}

	@Override
	public boolean hasExtraData() {
		return true;
	}

	@Override
	public String getExtraData(int position) {
		return String.valueOf(position);
	}

	private void changeLevel(View view) {
		if (currentLevel == LEVEL_0) {
			currentLevel = LEVEL_1;
			((android.widget.Button) view).setTextColor(Color.YELLOW);
		} else if ((currentLevel == LEVEL_1)) {
			currentLevel = LEVEL_2;
			((android.widget.Button) view).setTextColor(Color.RED);
		} else {
			currentLevel = LEVEL_0;
			((android.widget.Button) view).setTextColor(Color.WHITE);
		}
		sharedPreferences.edit().putInt("seat" + seat + "level", currentLevel).apply();
	}
}
