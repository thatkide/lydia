package com.autosenseapp.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

/**
 * Created by eric on 2014-09-02.
 */
public class PinTriggersDataSource {

	private SQLiteDatabase database;
	private PinTriggersOpenHelper dbHelper;

	private static final String TAG = PinTriggersDataSource.class.getSimpleName();

	public PinTriggersDataSource(Context context) {
		dbHelper = new PinTriggersOpenHelper(context);
	}

	public void open() throws SQLiteException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}


}
