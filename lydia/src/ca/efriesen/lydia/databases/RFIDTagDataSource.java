package ca.efriesen.lydia.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;

/**
 * Created by eric on 2013-08-18.
 */
public class RFIDTagDataSource {
	private SQLiteDatabase database;
	private RFIDTagOpenHelper dbHelper;

	private static final String TAG = "lydia rfid tag database";

	public RFIDTagDataSource(Context context) {
		dbHelper = new RFIDTagOpenHelper(context);
	}

	public void open() throws SQLiteException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public long addTag(RFIDTag tag) {
		ContentValues values = new ContentValues();
		values.put(RFIDTagOpenHelper.TAGNUMBER, tag.getTagNumber());
		values.put(RFIDTagOpenHelper.DESCRIPTION, tag.getDescription());
		values.put(RFIDTagOpenHelper.ENABLED, tag.getEnabled());

		return database.insert(RFIDTagOpenHelper.TABLE_NAME, null, values);
	}

	public int removeTag(RFIDTag tag) {
		return database.delete(RFIDTagOpenHelper.TABLE_NAME, RFIDTagOpenHelper.COLUMN_ID + " = " + tag.getId(), null);
	}

	public ArrayList<RFIDTag> getAllTags() {
		ArrayList<RFIDTag> tags = new ArrayList<RFIDTag>();

		Cursor cursor = database.query(
				RFIDTagOpenHelper.TABLE_NAME,
				new String[] {RFIDTagOpenHelper.COLUMN_ID, RFIDTagOpenHelper.TAGNUMBER, RFIDTagOpenHelper.DESCRIPTION, RFIDTagOpenHelper.ENABLED},
				null,
				null,
				null,
				null,
				null
		);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			RFIDTag tag = cursorToTag(cursor);
			tags.add(tag);
			cursor.moveToNext();
		}
		return tags;
	}

	private RFIDTag cursorToTag(Cursor cursor) {
		RFIDTag tag = new RFIDTag();
		tag.setId(cursor.getLong(cursor.getColumnIndex(RFIDTagOpenHelper.COLUMN_ID)));
		tag.setTagNumber(cursor.getLong(cursor.getColumnIndex(RFIDTagOpenHelper.TAGNUMBER)));
		tag.setDescription(cursor.getString(cursor.getColumnIndex(RFIDTagOpenHelper.DESCRIPTION)));
		tag.setEnabled(cursor.getInt(cursor.getColumnIndex(RFIDTagOpenHelper.ENABLED)) != 0);

		return tag;
	}


}
