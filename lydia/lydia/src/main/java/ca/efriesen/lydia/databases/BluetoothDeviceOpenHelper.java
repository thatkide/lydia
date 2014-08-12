package ca.efriesen.lydia.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by eric on 2013-08-02.
 */
public class BluetoothDeviceOpenHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "bluetooth_devices.db";

	public static final String TABLE_NAME = "bluetooth_devices";

	public static final String COLUMN_ID = "_id";
	public static final String ADDRESS = "address";

	private static final String TABLE_CREATE =
			"CREATE TABLE " + TABLE_NAME +
					" (" + COLUMN_ID + " integer primary key autoincrement, " +
					ADDRESS + " text not null); ";


	BluetoothDeviceOpenHelper(Context context) {
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
