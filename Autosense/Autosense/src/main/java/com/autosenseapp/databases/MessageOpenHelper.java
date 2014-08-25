package com.autosenseapp.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by eric on 2013-06-02.
 */
public class MessageOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "messages.db";

	public static final String TABLE_NAME = "messages";

	public static final String COLUMN_ID = "_id";
	public static final String MESSAGE = "message";
	public static final String PHONENUMBER = "phone_number";
	public static final String TIMERECEIVED = "time_received";
	public static final String TYPE = "type";
	public static final String FROMME = "from_me";

	private static final String TABLE_CREATE =
			"CREATE TABLE " + TABLE_NAME +
					" (" + COLUMN_ID + " integer primary key autoincrement, " +
					MESSAGE + " text, " +
					PHONENUMBER + " text not null, " +
					TIMERECEIVED + " integer not null, " +
					TYPE + " text not null, " +
					FROMME + " integer not null);";


	MessageOpenHelper(Context context) {
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
