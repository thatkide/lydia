package com.autosenseapp.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.autosenseapp.devices.actions.Action;
import com.autosenseapp.devices.triggers.Trigger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric on 2014-09-02.
 */
public class PinTriggersDataSource {

	private SQLiteDatabase database;
//	private PinTriggersOpenHelper dbHelper;
//	private PinTriggerController pinTriggerController;

	private static final String TAG = PinTriggersDataSource.class.getSimpleName();

	public PinTriggersDataSource(Context context) {
//		dbHelper = new PinTriggersOpenHelper(context);
//		pinTriggerController = (PinTriggerController) ((GlobalClass)context).getController(GlobalClass.PIN_TRIGGER_CONTROLLER);
	}

	public void open() throws SQLiteException {
		if (database == null || !database.isOpen()) {
//			database = dbHelper.getWritableDatabase();
		}
	}

	public void close() {
//		dbHelper.close();
	}

	public void addPinTrigger(ArduinoPin arduinoPin, Trigger trigger, Action action) {
		removePinTrigger(arduinoPin, trigger);
		open();
		ContentValues values = new ContentValues();
//		values.put(PinTriggersOpenHelper.PIN_ID, arduinoPin.getId());
//		values.put(PinTriggersOpenHelper.ACTION_ID, action.getId());
//		values.put(PinTriggersOpenHelper.TRIGGER_ID, trigger.getId());
//		values.put(PinTriggersOpenHelper.EXTRA_DATA, "");

		// store it in the db
//		database.insert(PinTriggersOpenHelper.TABLE_NAME, null, values);

		close();
	}

	public void editPinTrigger(ArduinoPin arduinoPin, Trigger trigger, Action action) {
		open();
		ContentValues values = new ContentValues();
//		values.put(PinTriggersOpenHelper.PIN_ID, arduinoPin.getId());
//		values.put(PinTriggersOpenHelper.ACTION_ID, action.getId());
//		values.put(PinTriggersOpenHelper.TRIGGER_ID, trigger.getId());
//		values.put(PinTriggersOpenHelper.EXTRA_DATA, action.getExtraData());

//		database.update(PinTriggersOpenHelper.TABLE_NAME, values,
//				PinTriggersOpenHelper.PIN_ID + "=? AND " +
//						PinTriggersOpenHelper.TRIGGER_ID + " =?",
//				new String[]{String.valueOf(arduinoPin.getId()), trigger.getId()});
		close();
	}

	public void removePinTrigger(ArduinoPin arduinoPin, Trigger trigger) {
		open();
//		database.delete(PinTriggersOpenHelper.TABLE_NAME,
//				PinTriggersOpenHelper.PIN_ID + "=? AND " +
//				PinTriggersOpenHelper.TRIGGER_ID + " =?",
//				new String[]{String.valueOf(arduinoPin.getId()), trigger.getId()});
		close();
	}

	public List<PinTrigger> getPinTriggers(ArduinoPin arduinoPin) {
		open();
		List<PinTrigger> triggers = new ArrayList<PinTrigger>();
//		Cursor cursor = database.query(
//				PinTriggersOpenHelper.TABLE_NAME,
//				null, // all columns
//				PinTriggersOpenHelper.PIN_ID + " = ?", // selection
//				new String[] {String.valueOf(arduinoPin.getId())}, // selectionArgs
//				null, // groupBy
//				null, // having
//				null  // orderBy
//		);
//		cursor.moveToFirst();
//		while (!cursor.isAfterLast()) {
//			PinTrigger pinTrigger = cursorToPin(cursor);
//			cursor.moveToNext();
//		}
//		cursor.close();
//		cursor.close();
		return triggers;
	}


	private PinTrigger cursorToPin(Cursor cursor) {
		PinTrigger pinTrigger = new PinTrigger();
//		pinTrigger.setId(cursor.getInt(cursor.getColumnIndex(PinTriggersOpenHelper.COLUMN_ID)));
//		String actionId = cursor.getString(cursor.getColumnIndex(PinTriggersOpenHelper.ACTION_ID));
//		pinTrigger.setAction(pinTriggerController.getAction(actionId));

		return pinTrigger;
	}
}


