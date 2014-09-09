package com.autosenseapp.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.autosenseapp.devices.actions.Action;
import com.autosenseapp.devices.triggers.Trigger;
import com.autosenseapp.services.ArduinoService;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric on 2014-09-05.
 */
public class ArduinoPinsDataSource {

	private SQLiteDatabase database;
	private ArduinoPinsOpenHelper dbHelper;
	private String DEVICE_TABLE;
	private Context context;

	private static final String TAG = ArduinoPinsDataSource.class.getSimpleName();

	public ArduinoPinsDataSource(Context context) {
		dbHelper = new ArduinoPinsOpenHelper(context);
		this.context = context;
	}

	public void open() throws SQLiteException {
		if (database == null || !database.isOpen()) {
			database = dbHelper.getWritableDatabase();
		}

		int deviceType = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS).getInt(ArduinoService.ARDUINO_TYPE, ArduinoService.ARDUINO_NONE);
		if (deviceType == ArduinoService.ARDUINO_ACCESSORY) {
			this.DEVICE_TABLE = ArduinoPinsOpenHelper.ACCESSORY_TABLE;
		} else if (deviceType == ArduinoService.ARDUINO_DEVICE) {
			this.DEVICE_TABLE = ArduinoPinsOpenHelper.DEVICE_TABLE;
		} else {
			close();
		}
	}

	public void close() {
		dbHelper.close();
	}

	public List<ArduinoPin> getAnalogPins() {
		return getPins(ArduinoPin.ANALOG);
	}

	public List<ArduinoPin> getDigitalPins() {
		return getPins(ArduinoPin.DIGITAL);
	}

	public List<ArduinoPin> getPins(int pinType) {
		List<ArduinoPin> arduinoPins = new ArrayList<ArduinoPin>();
		Cursor cursor = database.query(
				DEVICE_TABLE,
				null, // all columns
				ArduinoPinsOpenHelper.TYPE + " = " + pinType, // selection
				null, // selectionArgs
				null, // groupBy
				null, // having
				ArduinoPinsOpenHelper.NUMBER  // orderBy
		);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ArduinoPin pin = cursorToPin(cursor);
			pin.setTriggers(getTriggers(pin));
			arduinoPins.add(pin);
			cursor.moveToNext();
		}
		cursor.close();
		return arduinoPins;
	}

	public List<Action> getActions() {
		open();
		List<Action> actions = new ArrayList<Action>();
		Cursor cursor = database.query(
				ArduinoPinsOpenHelper.ACTIONS_TABLE,
				new String[]{
						ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID + " as " + ArduinoPinsOpenHelper.ACTION_ID,
						ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.CLASS + " as " + ArduinoPinsOpenHelper.ACTION_CLASS,
						ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.NAME + " as " + ArduinoPinsOpenHelper.ACTION_NAME
				}, // all columns
				null, // selection
				null, // selectionArgs
				null, // groupBy
				null, // having
				ArduinoPinsOpenHelper.NAME  // orderBy
		);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Action action = cursorToAction(cursor);
			actions.add(action);
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return actions;
	}

	public List<Trigger> getTriggers() {
		open();
		List<Trigger> triggers = new ArrayList<Trigger>();
		Cursor cursor = database.query(
				ArduinoPinsOpenHelper.TRIGGERS_TABLE,
				new String[]{
					ArduinoPinsOpenHelper.TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID + " as " + ArduinoPinsOpenHelper.TRIGGER_ID,
					ArduinoPinsOpenHelper.TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.CLASS + " as " + ArduinoPinsOpenHelper.TRIGGER_CLASS,
					ArduinoPinsOpenHelper.TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.NAME + " as " + ArduinoPinsOpenHelper.TRIGGER_NAME
				}, // all columns
				null, // selection
				null, // selectionArgs
				null, // groupBy
				null, // having
				ArduinoPinsOpenHelper.NAME  // orderBy
		);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Trigger trigger = cursorToTrigger(cursor);
			triggers.add(trigger);
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return triggers;
	}

	private List<Trigger> getTriggers(ArduinoPin pin) {
		List<Trigger> triggers = new ArrayList<Trigger>();
		String query = "SELECT " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + ".*, " +
				ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID + " as " + ArduinoPinsOpenHelper.ACTION_ID + ", " +
				ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.CLASS + " as " + ArduinoPinsOpenHelper.ACTION_CLASS + ", " +
				ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.NAME + " as " + ArduinoPinsOpenHelper.ACTION_NAME + ", " +
				ArduinoPinsOpenHelper.TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID + " as " + ArduinoPinsOpenHelper.TRIGGER_ID + ", " +
				ArduinoPinsOpenHelper.TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.CLASS + " as " + ArduinoPinsOpenHelper.TRIGGER_CLASS + ", " +
				ArduinoPinsOpenHelper.TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.NAME + " as " + ArduinoPinsOpenHelper.TRIGGER_NAME +
				" FROM " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE +
				" LEFT JOIN " + ArduinoPinsOpenHelper.ACTIONS_TABLE + " ON " +
					ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID + " = " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.ACTION_ID +
				" LEFT JOIN " + ArduinoPinsOpenHelper.TRIGGERS_TABLE + " ON " +
					ArduinoPinsOpenHelper.TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID + " = " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.TRIGGER_ID +
				" WHERE " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.PIN_ID + "=?";

		Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(pin.getId())});
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String actionName = cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.ACTION_NAME));
			String actionClass = cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.ACTION_CLASS));
			if (actionClass != null) {
				Action action = (Action) createClass(ArduinoPinsOpenHelper.ACTIONS_TABLE, actionClass);
				action.setId(cursor.getInt(cursor.getColumnIndex(ArduinoPinsOpenHelper.ACTION_ID)));
				action.setName(actionName);

				Trigger trigger = cursorToTrigger(cursor);
				trigger.setAction(action);
				triggers.add(trigger);
			}
			cursor.moveToNext();
		}
		return triggers;
	}

	public void updatePin(ArduinoPin arduinoPin) {
		open();
		ContentValues values = new ContentValues();
		values.put(ArduinoPinsOpenHelper.MODE, arduinoPin.getMode());
		values.put(ArduinoPinsOpenHelper.NAME, arduinoPin.getPinName());
		values.put(ArduinoPinsOpenHelper.NUMBER, arduinoPin.getPinNumber());
		values.put(ArduinoPinsOpenHelper.TYPE, arduinoPin.getPinType());

		// store it in the db
		database.update(DEVICE_TABLE, values, ArduinoPinsOpenHelper.COLUMN_ID + " = " + arduinoPin.getId(), null);
		close();
	}

	private ArduinoPin cursorToPin(Cursor cursor) {
		ArduinoPin arduinoPin = new ArduinoPin();
		arduinoPin.setId(cursor.getInt(cursor.getColumnIndex(ArduinoPinsOpenHelper.COLUMN_ID)));
		arduinoPin.setMode(cursor.getInt(cursor.getColumnIndex(ArduinoPinsOpenHelper.MODE)));
		arduinoPin.setPinName(cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.NAME)));
		arduinoPin.setPinNumber(cursor.getInt(cursor.getColumnIndex(ArduinoPinsOpenHelper.NUMBER)));
		arduinoPin.setPinType(cursor.getInt(cursor.getColumnIndexOrThrow(ArduinoPinsOpenHelper.TYPE)));

		return arduinoPin;
	}

	private Action cursorToAction(Cursor cursor) {
		String actionClass = cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.ACTION_CLASS));
		String actionName = cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.ACTION_NAME));
		if (actionClass != null) {
			Action action = (Action) createClass(ArduinoPinsOpenHelper.ACTIONS_TABLE, actionClass);
			action.setId(cursor.getInt(cursor.getColumnIndex(ArduinoPinsOpenHelper.ACTION_ID)));
			action.setName(actionName);
			return action;
		}
		return null;
	}

	private Trigger cursorToTrigger(Cursor cursor) {
		String triggerClass = cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.TRIGGER_CLASS));
		String triggerName = cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.TRIGGER_NAME));
		if (triggerClass != null) {
			Trigger trigger = (Trigger) createClass(ArduinoPinsOpenHelper.TRIGGERS_TABLE, triggerClass);
			trigger.setId(cursor.getInt(cursor.getColumnIndex(ArduinoPinsOpenHelper.TRIGGER_ID)));
			trigger.setName(triggerName);
			return trigger;
		}
		return null;
	}

	private Object createClass(String directory, String clazzName) {
		try {
			Class<?> clazz = Class.forName(context.getPackageName() + ".devices." + directory + "." + clazzName);
			Constructor<?> constructor = clazz.getConstructor();
			return constructor.newInstance();
		} catch (Exception e) {}
		return null;
	}
}
