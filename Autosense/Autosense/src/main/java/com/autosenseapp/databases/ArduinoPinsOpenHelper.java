package com.autosenseapp.databases;

import android.content.Context;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by eric on 2014-09-05.
 */
public class ArduinoPinsOpenHelper extends SQLiteAssetHelper {

	private static final String TAG = ArduinoPinsOpenHelper.class.getSimpleName();

	public static final String ACCESSORY_TABLE = "arduino_accessory";
	public static final String DEVICE_TABLE = "arduino_device";
	public static final String ACTIONS_TABLE = "actions";
	public static final String PIN_TRIGGERS_TABLE = "pin_triggers";
	public static final String TRIGGERS_TABLE = "triggers";

	public static final String COLUMN_ID = "_id";
	public static final String MODE = "mode";
	public static final String NAME = "name";
	public static final String NUMBER = "number";
	public static final String TYPE = "type";
	public static final String EXTRA_DATA = "extra_data";
	public static final String COMMENT = "comment";
	public static final String TRIGGER_ID = "trigger_id";
	public static final String ACTION_ID = "action_id";
	public static final String ACTION_EXTRA_DATA = "action_extra";
	public static final String CLASS = "class";
	public static final String PIN_ID = "pin_id";
	public static final String PIN_NAME = "pin_name";
	public static final String PIN_TRIGGER_ID = "pin_trigger_id";
	public static final String ACTION_CLASS = "action_class";
	public static final String ACTION_NAME = "action_name";
	public static final String TRIGGER_CLASS = "trigger_class";
	public static final String TRIGGER_NAME = "trigger_name";


	private static final int VERSION = 5;
	private static String DB_NAME = "arduinoPins.db";

	public ArduinoPinsOpenHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
		this.setForcedUpgrade(VERSION);
	}
}
