package ca.efriesen.lydia.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import ca.efriesen.lydia.controllers.ButtonControllers.SettingsButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric on 2014-06-14.
 */
public class ButtonConfigDataSource {

	private SQLiteDatabase database;
	private ButtonConfigOpenHelper dbHelper;
	private String[] PROJECTION = {ButtonConfigOpenHelper.COLUMN_ID, ButtonConfigOpenHelper.DISPLAYAREA, ButtonConfigOpenHelper.POSITION, ButtonConfigOpenHelper.TITLE, ButtonConfigOpenHelper.ACTION, ButtonConfigOpenHelper.DRAWABLE, ButtonConfigOpenHelper.USESDRAWABLE};

	private static final String TAG = "button config ";

	public ButtonConfigDataSource(Context context) {
		dbHelper = new ButtonConfigOpenHelper(context);
	}

	public void open() throws SQLiteException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public List<Button> getButtonsInArea(int area) {
		List<Button> buttons = new ArrayList<Button>();
		Cursor cursor = database.query(ButtonConfigOpenHelper.TABLE_NAME, PROJECTION, ButtonConfigOpenHelper.DISPLAYAREA + " = " + area, null, null, null, ButtonConfigOpenHelper.POSITION + " ASC");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Button button = cursorToButton(cursor);
			buttons.add(button);
			cursor.moveToNext();
		}
		cursor.close();
		return buttons;
	}

	public Button getButton(int area, int position) {
		Cursor cursor = database.query(ButtonConfigOpenHelper.TABLE_NAME, PROJECTION, ButtonConfigOpenHelper.DISPLAYAREA + " = " + area + " AND " + ButtonConfigOpenHelper.POSITION + " = " + position, null, null, null, null);
		cursor.moveToFirst();
		Button button = cursorToButton(cursor);
		cursor.close();
		return button;
	}

	public long addButton(Button button) {
		ContentValues values = new ContentValues();
		values.put(ButtonConfigOpenHelper.DISPLAYAREA, button.getDisplayArea());
		values.put(ButtonConfigOpenHelper.POSITION, button.getPosition());
		values.put(ButtonConfigOpenHelper.TITLE, button.getTitle());
		values.put(ButtonConfigOpenHelper.ACTION, button.getAction());
		values.put(ButtonConfigOpenHelper.DRAWABLE, button.getDrawable());
		values.put(ButtonConfigOpenHelper.USESDRAWABLE, (button.getUsesDrawable() ? 1 : 0));

		long insertId = database.insert(ButtonConfigOpenHelper.TABLE_NAME, null, values);
		Log.d(TAG, "new button added, id is " + insertId);
		return insertId;
	}

	public void editButton(Button button) {
		// add new button instead of edit
		if (!(button.getId() > 0)) {
			addButton(button);
		} else {
			// update all the values
			ContentValues values = new ContentValues();
			values.put(ButtonConfigOpenHelper.DISPLAYAREA, button.getDisplayArea());
			values.put(ButtonConfigOpenHelper.POSITION, button.getPosition());
			values.put(ButtonConfigOpenHelper.TITLE, button.getTitle());
			values.put(ButtonConfigOpenHelper.ACTION, button.getAction());
			values.put(ButtonConfigOpenHelper.DRAWABLE, button.getDrawable());
			values.put(ButtonConfigOpenHelper.USESDRAWABLE, (button.getUsesDrawable() ? 1 : 0));

			// store it in the db
			database.update(ButtonConfigOpenHelper.TABLE_NAME, values, ButtonConfigOpenHelper.COLUMN_ID + " = " + button.getId(), null);
		}
	}

	public boolean hasSettingsButton() {
		Cursor cursor = database.query(ButtonConfigOpenHelper.TABLE_NAME, PROJECTION, ButtonConfigOpenHelper.ACTION + " = " + DatabaseUtils.sqlEscapeString(SettingsButton.ACTION), null, null, null, null);
		cursor.moveToFirst();
		boolean hasButton = cursor.getCount() > 0;
		cursor.close();
		return hasButton;
	}

	public void removeButton(Button button) {
		database.delete(ButtonConfigOpenHelper.TABLE_NAME, ButtonConfigOpenHelper.DISPLAYAREA + " = " + button.getDisplayArea() + " AND " + ButtonConfigOpenHelper.POSITION + " = " + button.getPosition(), null);
	}

	private Button cursorToButton(Cursor cursor) {
		Button button = new Button();
		button.setId(cursor.getInt(cursor.getColumnIndex(ButtonConfigOpenHelper.COLUMN_ID)));
		button.setDisplayArea(cursor.getInt(cursor.getColumnIndex(ButtonConfigOpenHelper.DISPLAYAREA)));
		button.setPosition(cursor.getInt(cursor.getColumnIndex(ButtonConfigOpenHelper.POSITION)));
		button.setTitle(cursor.getString(cursor.getColumnIndex(ButtonConfigOpenHelper.TITLE)));
		button.setAction(cursor.getString(cursor.getColumnIndex(ButtonConfigOpenHelper.ACTION)));
		button.setDrawable(cursor.getString(cursor.getColumnIndex(ButtonConfigOpenHelper.DRAWABLE)));
		button.setUsesDrawable(cursor.getInt(cursor.getColumnIndex(ButtonConfigOpenHelper.USESDRAWABLE)) > 0);

		return button;
	}
}

