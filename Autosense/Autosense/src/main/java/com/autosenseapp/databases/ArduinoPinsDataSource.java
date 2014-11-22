package com.autosenseapp.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.autosenseapp.controllers.ArduinoController;
import com.autosenseapp.devices.Master;
import com.autosenseapp.devices.actions.Action;
import com.autosenseapp.devices.outputTriggers.Trigger;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
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
		int deviceType = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS).getInt(ArduinoController.ARDUINO_TYPE, ArduinoController.ARDUINO_NONE);
		if (deviceType == ArduinoController.ARDUINO_ACCESSORY) {
			this.DEVICE_TABLE = ArduinoPinsOpenHelper.ACCESSORY_TABLE;
		} else if (deviceType == ArduinoController.ARDUINO_DEVICE) {
			this.DEVICE_TABLE = ArduinoPinsOpenHelper.DEVICE_TABLE;
		}
	}

	private boolean hasValidDevice() {
		int deviceType = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS).getInt(ArduinoController.ARDUINO_TYPE, ArduinoController.ARDUINO_NONE);
		if (deviceType != ArduinoController.ARDUINO_NONE) {
			return true;
		} else {
			return false;
		}
	}

	public void close() {
		dbHelper.close();
	}

	public void editPinTrigger(ArduinoPin arduinoPin, Trigger trigger, Action action) {
		open();
		ContentValues values = new ContentValues();
		values.put(ArduinoPinsOpenHelper.PIN_ID, arduinoPin.getId());
		values.put(ArduinoPinsOpenHelper.ACTION_ID, action.getId());
		values.put(ArduinoPinsOpenHelper.TRIGGER_ID, trigger.getId());
		values.put(ArduinoPinsOpenHelper.EXTRA_DATA, action.getExtraData());

		// try an update first
		int numrows = database.update(ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE, values,
				ArduinoPinsOpenHelper.PIN_ID + "=? AND " +
				ArduinoPinsOpenHelper.TRIGGER_ID + " =?",
				new String[]{String.valueOf(arduinoPin.getId()), String.valueOf(trigger.getId())});
		// if we can't update, insert new
		if (numrows == 0) {
			database.insert(ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE, null, values);
		}
	}

	public List<ArduinoPin> getAllTriggersByClassName(String name) {
		if (!hasValidDevice()) {
			return new ArrayList<ArduinoPin>();
		}
		List<ArduinoPin> pins = new ArrayList<ArduinoPin>();
		open();
		String query = "SELECT " +
				ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID + " as " + ArduinoPinsOpenHelper.ACTION_ID + ", " +
				ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.CLASS + " as " + ArduinoPinsOpenHelper.ACTION_CLASS + ", " +
				ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.NAME + " as " + ArduinoPinsOpenHelper.ACTION_NAME + ", " +
				ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID + " as " + ArduinoPinsOpenHelper.PIN_TRIGGER_ID + ", " +
				"* FROM " + ArduinoPinsOpenHelper.TRIGGERS_TABLE +
				" INNER JOIN " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE +
					" ON " + ArduinoPinsOpenHelper.TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID +
					" = " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.TRIGGER_ID +
				" INNER JOIN " + DEVICE_TABLE +
					" ON " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.PIN_ID +
					" = " + DEVICE_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID +
				" INNER JOIN " + ArduinoPinsOpenHelper.ACTIONS_TABLE +
					" ON " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.ACTION_ID +
					" = " + ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID +
				" WHERE " + ArduinoPinsOpenHelper.TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.CLASS + "=" + DatabaseUtils.sqlEscapeString(name);

		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			ArduinoPin pin = cursorToPin(cursor);
			// need to overwrite id, name,
			// overwrite the id with the correct column
			pin.setId(cursor.getInt(cursor.getColumnIndex(ArduinoPinsOpenHelper.PIN_ID)));
			pin.setAction(cursorToAction(cursor));
			pins.add(pin);

			cursor.moveToNext();
		}

		cursor.close();
		return pins;
	}

	public List<ArduinoPin> getPins(int pinType) {
		open();
		List<ArduinoPin> arduinoPins = new ArrayList<ArduinoPin>();
		Cursor cursor = database.query(
				DEVICE_TABLE,
				null, // all columns
				ArduinoPinsOpenHelper.TYPE + " =? ", // selection
				new String[] {String.valueOf(pinType)}, // selectionArgs
				null, // groupBy
				null, // having
				ArduinoPinsOpenHelper.NUMBER  // orderBy
		);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ArduinoPin pin = cursorToPin(cursor);
//			pin.setTriggers(getOutputTriggers(pin));
			arduinoPins.add(pin);
			cursor.moveToNext();
		}
		cursor.close();
		return arduinoPins;
	}

	public ArduinoPin getPinTriggerById(int id) {
		open();
		String query = "SELECT " +
				ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID + " as " + ArduinoPinsOpenHelper.ACTION_ID + ", " +
				ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.CLASS + " as " + ArduinoPinsOpenHelper.ACTION_CLASS + ", " +
				ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.NAME + " as " + ArduinoPinsOpenHelper.ACTION_NAME + ", " +
				ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID + " as " + ArduinoPinsOpenHelper.PIN_TRIGGER_ID + ", " +
				"* FROM " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE +
				" INNER JOIN " + ArduinoPinsOpenHelper.TRIGGERS_TABLE +
				" ON " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.TRIGGER_ID +
				" = " + ArduinoPinsOpenHelper.TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID +
				" INNER JOIN " + DEVICE_TABLE +
				" ON " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.PIN_ID +
				" = " + DEVICE_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID +
				" INNER JOIN " + ArduinoPinsOpenHelper.ACTIONS_TABLE +
				" ON " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.ACTION_ID +
				" = " + ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID +
				" WHERE " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID + "=" + DatabaseUtils.sqlEscapeString(String.valueOf(id));

		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();

		ArduinoPin pin = cursorToPin(cursor);
		// need to overwrite id, name,
		// overwrite the id with the correct column
		pin.setId(cursor.getInt(cursor.getColumnIndex(ArduinoPinsOpenHelper.PIN_ID)));
		pin.setAction(cursorToAction(cursor));
		pin.setExtraData(cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.EXTRA_DATA)));
		cursor.close();
		return pin;
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
		return Collections.unmodifiableList(actions);
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
		return Collections.unmodifiableList(triggers);
	}

	public List<Trigger> getTriggers(ArduinoPin pin) {
		open();
		List<Trigger> triggers = new ArrayList<Trigger>();
		String query = "SELECT " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + ".*, " +
				ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID + " as " + ArduinoPinsOpenHelper.ACTION_ID + ", " +
				ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.CLASS + " as " + ArduinoPinsOpenHelper.ACTION_CLASS + ", " +
				ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.NAME + " as " + ArduinoPinsOpenHelper.ACTION_NAME + ", " +
				ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.EXTRA_DATA + " as " + ArduinoPinsOpenHelper.ACTION_EXTRA_DATA + ", " +
				ArduinoPinsOpenHelper.TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID + " as " + ArduinoPinsOpenHelper.TRIGGER_ID + ", " +
				ArduinoPinsOpenHelper.TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.CLASS + " as " + ArduinoPinsOpenHelper.TRIGGER_CLASS + ", " +
				ArduinoPinsOpenHelper.TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.NAME + " as " + ArduinoPinsOpenHelper.TRIGGER_NAME +
				" FROM " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE +
				" LEFT JOIN " + ArduinoPinsOpenHelper.ACTIONS_TABLE + " ON " +
					ArduinoPinsOpenHelper.ACTIONS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID + " = " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.ACTION_ID +
				" LEFT JOIN " + ArduinoPinsOpenHelper.TRIGGERS_TABLE + " ON " +
					ArduinoPinsOpenHelper.TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.COLUMN_ID + " = " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.TRIGGER_ID +
				" WHERE " + ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE + "." + ArduinoPinsOpenHelper.PIN_ID + "=?" +
				" ORDER BY " + ArduinoPinsOpenHelper.TRIGGER_NAME;

		Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(pin.getId())});
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String actionClass = cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.ACTION_CLASS));
			if (actionClass != null) {
				Action action = (Action) createClass(ArduinoPinsOpenHelper.ACTIONS_TABLE, actionClass);
				action.setId(cursor.getInt(cursor.getColumnIndex(ArduinoPinsOpenHelper.ACTION_ID)));
				action.setExtraData(cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.ACTION_EXTRA_DATA)));
				action.setName(cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.ACTION_NAME)));

				Trigger trigger = cursorToTrigger(cursor);
				trigger.setAction(action);
				triggers.add(trigger);
			}
			cursor.moveToNext();
		}
		return triggers;
	}

	public void removePinTrigger(ArduinoPin arduinoPin, Trigger trigger) {
		if (arduinoPin == null || trigger == null) {
			return;
		}
		open();
		database.delete(ArduinoPinsOpenHelper.PIN_TRIGGERS_TABLE,
				ArduinoPinsOpenHelper.PIN_ID + "=? AND " +
				ArduinoPinsOpenHelper.TRIGGER_ID + " =?",
				new String[]{String.valueOf(arduinoPin.getId()), String.valueOf(trigger.getId())});
	}

	public void updatePin(ArduinoPin arduinoPin) {
		open();
		ContentValues values = new ContentValues();
		values.put(ArduinoPinsOpenHelper.MODE, arduinoPin.getMode());
		values.put(ArduinoPinsOpenHelper.NAME, arduinoPin.getPinName());
		values.put(ArduinoPinsOpenHelper.NUMBER, arduinoPin.getPinNumber());
		values.put(ArduinoPinsOpenHelper.TYPE, arduinoPin.getPinType());
		values.put(ArduinoPinsOpenHelper.COMMENT, arduinoPin.getComment());

		// store it in the db
		database.beginTransaction();
		try {
			database.update(DEVICE_TABLE, values, ArduinoPinsOpenHelper.COLUMN_ID + " =? ", new String[]{String.valueOf(arduinoPin.getId())});
			database.setTransactionSuccessful();
			// write it out to the arduino if successful
			byte data[] = {(byte) arduinoPin.getPinNumber(), (byte) arduinoPin.getMode()};
			Master.writeData(context, Master.PINMODE, data);
		} finally {
			database.endTransaction();
		}
	}

	private ArduinoPin cursorToPin(Cursor cursor) {
		ArduinoPin arduinoPin = new ArduinoPin();
		arduinoPin.setId(cursor.getInt(cursor.getColumnIndex(ArduinoPinsOpenHelper.COLUMN_ID)));
		arduinoPin.setMode(cursor.getInt(cursor.getColumnIndex(ArduinoPinsOpenHelper.MODE)));
		arduinoPin.setPinName(cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.NAME)));
		arduinoPin.setPinNumber(cursor.getInt(cursor.getColumnIndex(ArduinoPinsOpenHelper.NUMBER)));
		arduinoPin.setPinType(cursor.getInt(cursor.getColumnIndexOrThrow(ArduinoPinsOpenHelper.TYPE)));
		arduinoPin.setComment(cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.COMMENT)));

		if (cursor.getColumnIndex(ArduinoPinsOpenHelper.PIN_TRIGGER_ID) != -1) {
			arduinoPin.setPinTriggerId(cursor.getInt(cursor.getColumnIndex(ArduinoPinsOpenHelper.PIN_TRIGGER_ID)));
		}

		return arduinoPin;
	}

	private Action cursorToAction(Cursor cursor) {
		String actionClass = cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.ACTION_CLASS));
		String actionName = cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.ACTION_NAME));
		if (actionClass != null) {
			Action action = (Action) createClass(ArduinoPinsOpenHelper.ACTIONS_TABLE, actionClass);
			action.setId(cursor.getInt(cursor.getColumnIndex(ArduinoPinsOpenHelper.ACTION_ID)));
 			action.setName(actionName);
			action.setClassName(actionClass);
			return action;
		}
		return null;
	}

	private Trigger cursorToTrigger(Cursor cursor) {
		String triggerClass = cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.TRIGGER_CLASS));
		String triggerName = cursor.getString(cursor.getColumnIndex(ArduinoPinsOpenHelper.TRIGGER_NAME));
		if (triggerClass != null) {
			Trigger trigger = (Trigger) createClass("outputTriggers", triggerClass);
			trigger.setId(cursor.getInt(cursor.getColumnIndex(ArduinoPinsOpenHelper.TRIGGER_ID)));
			trigger.setName(triggerName);
			trigger.setClassName(triggerClass);
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
