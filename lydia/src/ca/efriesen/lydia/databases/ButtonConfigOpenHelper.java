package ca.efriesen.lydia.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by eric on 2014-06-14.
 */
public class ButtonConfigOpenHelper extends SQLiteOpenHelper {

	// If any schema changes are made, the hard coded settings button in the HomeScreenFragment will need updating
	private static final int DATABASE_VERSION = 6;
	private static final String DATABASE_NAME = "buttonConfig.db";

	public static final String TABLE_NAME = "button_config";

	public static final String COLUMN_ID = "_id";
	public static final String BUTTONTYPE = "button_type";
	public static final String DISPLAYAREA = "display_area";
	public static final String POSITION = "position";
	public static final String TITLE = "title";
	public static final String ACTION = "action";
	public static final String DRAWABLE = "drawable";
	public static final String USESDRAWABLE = "uses_drawable";

	private static final String TABLE_CREATE =
			"CREATE TABLE " + TABLE_NAME +
					" (" + COLUMN_ID + " integer primary key autoincrement, " +
					BUTTONTYPE + " integer not null, " +
					DISPLAYAREA + " integer not null, " +
					POSITION + " integer not null, " +
					TITLE + " text, " +
					ACTION + " text not null, " +
					DRAWABLE + " text not null, " +
					USESDRAWABLE + " integer not null);";


	ButtonConfigOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		sqLiteDatabase.execSQL(TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldversion, int newversion) {
		Log.w(MessageOpenHelper.class.getName(), "Upgrading Database from version " + oldversion + " to " + newversion + ".  This will destroy all data.");
		try {
			sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + BUTTONTYPE + " INTEGER DEFAULT 1 NOT NULL");
		} catch (Exception e) {
			// Column already exists
		}
//		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//		onCreate(sqLiteDatabase);
	}
}
