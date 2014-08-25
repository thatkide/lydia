package com.autosenseapp.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by eric on 2013-08-18.
 */
public class RFIDTagOpenHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 4;
	private static final String DATABASE_NAME = "rfid_tags.db";

	public static final String TABLE_NAME = "rfid_tags";

	public static final String COLUMN_ID = "_id";
	public static final String TAGNUMBER = "tag_number";
	public static final String DESCRIPTION = "description";
	public static final String ENABLED = "enabled";
	public static final String STARTCAR = "start_car";
	public static final String UNLOCKDOORS = "unlock_doors";
	public static final String EEPROMADDRESS = "eeprom_address";

	private static final String TABLE_CREATE =
			"CREATE TABLE " + TABLE_NAME +
					" (" + COLUMN_ID + " integer primary key autoincrement, " +
					TAGNUMBER + " integer not null unique, " +
					DESCRIPTION + " text, " +
					ENABLED + " integer not null, " +
					STARTCAR + " integer not null, " +
					UNLOCKDOORS + " integer not null, " +
					EEPROMADDRESS + " integer not null unique);";

	RFIDTagOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		sqLiteDatabase.execSQL(TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldversion, int newversion) {
		Log.w(MessageOpenHelper.class.getName(), "Upgrading Database from version " + oldversion + " to " + newversion + ".  This will destroy all data.");
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(sqLiteDatabase);
	}

}