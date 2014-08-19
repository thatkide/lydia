package ca.efriesen.lydia.databases;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import ca.efriesen.lydia_common.includes.Intents;

import java.util.ArrayList;

/**
 * Created by eric on 2013-08-18.
 */
public class RFIDTagDataSource {
	private SQLiteDatabase database;
	private RFIDTagOpenHelper dbHelper;
	private Context context;

	private static final String TAG = "lydia rfid tag database";

	public RFIDTagDataSource(Context context) {
		dbHelper = new RFIDTagOpenHelper(context);
		this.context = context;
	}

	public void open() throws SQLiteException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public long addTag(RFIDTag tag) {
		tag.setEepromAddress(getNextEepromAddress());
		ContentValues values = new ContentValues();
		values.put(RFIDTagOpenHelper.TAGNUMBER, tag.getTagNumber());
		values.put(RFIDTagOpenHelper.DESCRIPTION, tag.getDescription());
		values.put(RFIDTagOpenHelper.ENABLED, tag.getEnabled());
		values.put(RFIDTagOpenHelper.STARTCAR, tag.getStartCar());
		values.put(RFIDTagOpenHelper.UNLOCKDOORS, tag.getUnlockDoors());
		values.put(RFIDTagOpenHelper.EEPROMADDRESS, tag.getEepromAddress());

		long id = database.insert(RFIDTagOpenHelper.TABLE_NAME, null, values);
		// send a broadcast to the alarm class so it can write out the data over the wire
		context.sendBroadcast(new Intent(Intents.ALARM).putExtra("rfid_tag", tag));
		return id;
	}

	public int removeTag(RFIDTag tag) {
		int id =  database.delete(RFIDTagOpenHelper.TABLE_NAME, RFIDTagOpenHelper.COLUMN_ID + " = " + tag.getId(), null);
		if (id != 0) {
			// nullify all data
			tag.setEnabled(false);
			tag.setStartCar(false);
			tag.setUnlockDoors(false);
			tag.setTagNumber(0000000);
			context.sendBroadcast(new Intent(Intents.ALARM).putExtra("rfid_tag", tag));
		}
		return id;
	}

	public ArrayList<RFIDTag> getAllTags() {
		ArrayList<RFIDTag> tags = new ArrayList<RFIDTag>();

		Cursor cursor = database.query(
				RFIDTagOpenHelper.TABLE_NAME,
				new String[] {RFIDTagOpenHelper.COLUMN_ID, RFIDTagOpenHelper.TAGNUMBER, RFIDTagOpenHelper.DESCRIPTION, RFIDTagOpenHelper.ENABLED, RFIDTagOpenHelper.STARTCAR, RFIDTagOpenHelper.UNLOCKDOORS},
				null,
				null,
				null,
				null,
				RFIDTagOpenHelper.DESCRIPTION + " COLLATE NOCASE ASC"
		);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			RFIDTag tag = cursorToTag(cursor);
			tags.add(tag);
			cursor.moveToNext();
		}
		return tags;
	}

	public RFIDTag getTag(long id) {
		Cursor cursor = database.query(
				RFIDTagOpenHelper.TABLE_NAME,
				new String[] {RFIDTagOpenHelper.COLUMN_ID, RFIDTagOpenHelper.TAGNUMBER, RFIDTagOpenHelper.DESCRIPTION, RFIDTagOpenHelper.ENABLED, RFIDTagOpenHelper.STARTCAR, RFIDTagOpenHelper.UNLOCKDOORS},
				RFIDTagOpenHelper.COLUMN_ID + " = " + id,
				null,
				null,
				null,
				null
		);
		cursor.moveToFirst();
		RFIDTag tag = cursorToTag(cursor);
		cursor.close();
		return tag;
	}

	public void update(RFIDTag tag) {
		// update all the values
		ContentValues values = new ContentValues();
		values.put(RFIDTagOpenHelper.TAGNUMBER, tag.getTagNumber());
		values.put(RFIDTagOpenHelper.DESCRIPTION, tag.getDescription());
		values.put(RFIDTagOpenHelper.ENABLED, tag.getEnabled());
		values.put(RFIDTagOpenHelper.STARTCAR, tag.getStartCar());
		values.put(RFIDTagOpenHelper.UNLOCKDOORS, tag.getUnlockDoors());
		values.put(RFIDTagOpenHelper.EEPROMADDRESS, tag.getEepromAddress());

		// store it in the db
		database.update(RFIDTagOpenHelper.TABLE_NAME, values, RFIDTagOpenHelper.COLUMN_ID + " = " + tag.getId(), null);

		// send a broadcast to the alarm class so it can write out the data over the wire
		context.sendBroadcast(new Intent(Intents.ALARM).putExtra("rfid_tag", tag));
	}

	public int getNextEepromAddress() {
		int pos = 0;
		Cursor cursor = database.query(
				RFIDTagOpenHelper.TABLE_NAME,
				new String[] {RFIDTagOpenHelper.EEPROMADDRESS},
				null,
				null,
				null,
				null,
				RFIDTagOpenHelper.EEPROMADDRESS + " ASC"
 		);
		// start at the beginning
		cursor.moveToFirst();
		// if we don't have any tags, start at 0
		if (cursor.getCount() == 0) {
			pos = 0;
		} else {
			// loop over all the tags
			while (!cursor.isAfterLast()) {
				// get the address of the next one (remember, they're in numerical order)
				int address = cursor.getInt(cursor.getColumnIndex(RFIDTagOpenHelper.EEPROMADDRESS));
				// if the address doens't equal the position, we've found our next available address
				if (address != pos) {
					// stop looking
					break;
				}
				// keep incrementing with the loop
				pos += 4;
				// move the cursor
				cursor.moveToNext();
			}
		}
		// close the db
		cursor.close();
		// return
		return pos;
	}

	private RFIDTag cursorToTag(Cursor cursor) {
		RFIDTag tag = new RFIDTag();
  		tag.setId(cursor.getLong(cursor.getColumnIndex(RFIDTagOpenHelper.COLUMN_ID)));
		tag.setTagNumber(cursor.getLong(cursor.getColumnIndex(RFIDTagOpenHelper.TAGNUMBER)));
		tag.setDescription(cursor.getString(cursor.getColumnIndex(RFIDTagOpenHelper.DESCRIPTION)));
		tag.setEnabled(cursor.getInt(cursor.getColumnIndex(RFIDTagOpenHelper.ENABLED)) != 0);
		tag.setStartCar(cursor.getInt(cursor.getColumnIndex(RFIDTagOpenHelper.STARTCAR)) != 0);
		tag.setUnlockDoors(cursor.getInt(cursor.getColumnIndex(RFIDTagOpenHelper.UNLOCKDOORS)) != 0);

		return tag;
	}


}
