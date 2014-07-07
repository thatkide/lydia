package ca.efriesen.lydia.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import ca.efriesen.lydia.configs.AdminButtonsConfig;
import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.buttons.SettingsButton;
import ca.efriesen.lydia.callbacks.ButtonCheckerCallback;
import ca.efriesen.lydia.configs.NavigationButtonsConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by eric on 2014-06-14.
 */
public class ButtonConfigDataSource {

	private SQLiteDatabase database;
	private ButtonConfigOpenHelper dbHelper;
	private String[] PROJECTION = {ButtonConfigOpenHelper.COLUMN_ID, ButtonConfigOpenHelper.BUTTONTYPE, ButtonConfigOpenHelper.DISPLAYAREA, ButtonConfigOpenHelper.GROUP, ButtonConfigOpenHelper.POSITION, ButtonConfigOpenHelper.TITLE, ButtonConfigOpenHelper.ACTION, ButtonConfigOpenHelper.DRAWABLE, ButtonConfigOpenHelper.USESDRAWABLE, ButtonConfigOpenHelper.EXTRADATA};

	private static final String TAG = ButtonConfigDataSource.class.getSimpleName();

	public ButtonConfigDataSource(Context context) {
		dbHelper = new ButtonConfigOpenHelper(context);
	}

	public void open() throws SQLiteException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void checkRequiredButtons() {
		boolean doUpdate = false;

		List<ButtonCheckerCallback> configs = new ArrayList<ButtonCheckerCallback>();
		configs.add(new AdminButtonsConfig());
		configs.add(new NavigationButtonsConfig());

		for (ButtonCheckerCallback config : configs) {
			Log.d(TAG, "doing check for " + config.getGroup());
			List<Button> buttons = config.getButtons();
			List<Button> buttonsInDb = getButtonsInGroup(config.getType(), config.getGroup());

			// sort both lists by action
			Collections.sort(buttons, new Comparator<Button>() {
				@Override
				public int compare(Button button1, Button button2) {
					return button1.getAction().compareToIgnoreCase(button2.getAction());
				}
			});
			Collections.sort(buttonsInDb, new Comparator<Button>() {
				@Override
				public int compare(Button button1, Button button2) {
					return button1.getAction().compareToIgnoreCase(button2.getAction());
				}
			});


			if (buttons.size() != buttonsInDb.size()) {
				Log.d(TAG, "size difference");
				doUpdate = true;
			} else {
				for (int i=0; i<buttons.size(); i++) {
					if (!buttons.get(i).getAction().equalsIgnoreCase(buttonsInDb.get(i).getAction())) {
						Log.d(TAG, "action different");
						doUpdate = true;
					}
				}
			}

			if (doUpdate) {
				Log.d(TAG, "doiong update");
				removeScreen(config.getType(), config.getGroup());
				addButtons(buttons);
			}

		}
	}

	public void addButtons(List<Button> buttons) {
		for (Button button : buttons) {
			addButton(button);
		}
	}

	public long addButton(Button button) {
		ContentValues values = getContentValues(button);
		long insertId = database.insert(ButtonConfigOpenHelper.TABLE_NAME, null, values);
		return insertId;
	}

	public void editButton(Button button) {
		// add new button instead of edit
		if (!(button.getId() > 0)) {
			addButton(button);
		} else {
			// update all the values
			ContentValues values = getContentValues(button);
			// store it in the db
			database.update(ButtonConfigOpenHelper.TABLE_NAME, values, ButtonConfigOpenHelper.COLUMN_ID + " = " + button.getId(), null);
		}
	}

	public List<Button> getButtonsInGroup(int type, int group) {
		List<Button> buttons = new ArrayList<Button>();
		Cursor cursor = database.query(ButtonConfigOpenHelper.TABLE_NAME, PROJECTION,
				ButtonConfigOpenHelper.BUTTONTYPE + " = " + type + " AND " +
						ButtonConfigOpenHelper.GROUP + " = " + group,
				null, null, null, ButtonConfigOpenHelper.POSITION + " ASC");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Button button = cursorToButton(cursor);
			buttons.add(button);
			cursor.moveToNext();
		}
		cursor.close();
		return buttons;
	}

	public List<Button> getButtonsInArea(int type, int area, int group) {
		List<Button> buttons = new ArrayList<Button>();
		Cursor cursor = database.query(ButtonConfigOpenHelper.TABLE_NAME, PROJECTION,
				ButtonConfigOpenHelper.BUTTONTYPE + " = " + type + " AND " +
				ButtonConfigOpenHelper.DISPLAYAREA + " = " + area + " AND " +
				ButtonConfigOpenHelper.GROUP + " = " + group,
				null, null, null, ButtonConfigOpenHelper.POSITION + " ASC");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Button button = cursorToButton(cursor);
			buttons.add(button);
			cursor.moveToNext();
		}
		cursor.close();
		return buttons;
	}

	public int numScreensPerGroup(int type, int group) {
		int count = 0;
		try {
			Cursor cursor = database.query(ButtonConfigOpenHelper.TABLE_NAME, PROJECTION,
					ButtonConfigOpenHelper.BUTTONTYPE + " = " + type + " AND " +
					ButtonConfigOpenHelper.GROUP + " = " + group,
					null, ButtonConfigOpenHelper.DISPLAYAREA, null, null
			);
			cursor.moveToFirst();
			count = cursor.getCount();
			cursor.close();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return count;
	}

	public boolean hasSettingsButton() {
		Cursor cursor = database.query(ButtonConfigOpenHelper.TABLE_NAME, PROJECTION,
				ButtonConfigOpenHelper.ACTION + " = " + DatabaseUtils.sqlEscapeString(SettingsButton.class.getSimpleName()) +
				" AND " + ButtonConfigOpenHelper.GROUP + " = 0", null, null, null, null);
		cursor.moveToFirst();
		boolean hasButton = cursor.getCount() > 0;
		cursor.close();
		return hasButton;
	}

	public void removeButton(Button button) {
		database.delete(ButtonConfigOpenHelper.TABLE_NAME,
				ButtonConfigOpenHelper.DISPLAYAREA + " = " + button.getDisplayArea() + " AND " +
				ButtonConfigOpenHelper.BUTTONTYPE + " = " + button.getButtonType() + " AND " +
				ButtonConfigOpenHelper.POSITION + " = " + button.getPosition(), null);
	}

	public void removeScreen(int type, int group) {
		// remove all buttons on specified screen
		database.delete(ButtonConfigOpenHelper.TABLE_NAME,
				ButtonConfigOpenHelper.BUTTONTYPE + " = " + type + " AND " +
				ButtonConfigOpenHelper.GROUP + " = " + group,
				null);
	}

	public void removeScreen(int type, int screen, int group) {
		// remove all buttons on specified screen
		database.delete(ButtonConfigOpenHelper.TABLE_NAME,
				ButtonConfigOpenHelper.DISPLAYAREA + " = " + screen + " AND " +
				ButtonConfigOpenHelper.BUTTONTYPE + " = " + type + " AND " +
				ButtonConfigOpenHelper.GROUP + " = " + group,
				null);
	}

	public void removeScreen(int type, int screen, int group, int numScreens) {
		removeScreen(type, screen, group);
		// move the rest of the buttons down one position

		for (int i=screen; i<numScreens; i++) {
			// move 3-2, 4-3, etc...
			ContentValues values = new ContentValues();
			values.put(ButtonConfigOpenHelper.DISPLAYAREA, i);
			database.update(ButtonConfigOpenHelper.TABLE_NAME, values, ButtonConfigOpenHelper.DISPLAYAREA + " = " + (i+1), null);
		}
	}

	public void switchButtons(Button button, int pos) {
		Cursor cursor = database.query(ButtonConfigOpenHelper.TABLE_NAME, PROJECTION,
				ButtonConfigOpenHelper.DISPLAYAREA + " = " + button.getDisplayArea() + " AND " +
				ButtonConfigOpenHelper.BUTTONTYPE + " = " + button.getButtonType() + " AND " +
				ButtonConfigOpenHelper.POSITION + " IN (" + button.getPosition() +", " + pos + ")" ,
				null, null, null, null);
		if (cursor.getCount() == 2) {
			cursor.moveToFirst();
			// get the buttons
			Button button1 = cursorToButton(cursor);
			cursor.moveToNext();
			Button button2 = cursorToButton(cursor);
			// save the positions
			int button1Pos = button1.getPosition();
			int button2Pos = button2.getPosition();
			// swap them
			button1.setPosition(button2Pos);
			button2.setPosition(button1Pos);
			// save them
			editButton(button1);
			editButton(button2);
		} else {
			button.setPosition(pos);
			editButton(button);
		}
	}

	private Button cursorToButton(Cursor cursor) {
		Button button = new Button();
		button.setId(cursor.getInt(cursor.getColumnIndex(ButtonConfigOpenHelper.COLUMN_ID)));
		button.setButtonType(cursor.getInt(cursor.getColumnIndex(ButtonConfigOpenHelper.BUTTONTYPE)));
		button.setDisplayArea(cursor.getInt(cursor.getColumnIndex(ButtonConfigOpenHelper.DISPLAYAREA)));
		button.setGroup(cursor.getInt(cursor.getColumnIndex(ButtonConfigOpenHelper.GROUP)));
		button.setPosition(cursor.getInt(cursor.getColumnIndex(ButtonConfigOpenHelper.POSITION)));
		button.setTitle(cursor.getString(cursor.getColumnIndex(ButtonConfigOpenHelper.TITLE)));
		button.setAction(cursor.getString(cursor.getColumnIndex(ButtonConfigOpenHelper.ACTION)));
		button.setDrawable(cursor.getString(cursor.getColumnIndex(ButtonConfigOpenHelper.DRAWABLE)));
		button.setUsesDrawable(cursor.getInt(cursor.getColumnIndex(ButtonConfigOpenHelper.USESDRAWABLE)) > 0);
		button.setExtraData(cursor.getString(cursor.getColumnIndex(ButtonConfigOpenHelper.EXTRADATA)));

		return button;
	}

	private ContentValues getContentValues(Button button) {
		ContentValues values = new ContentValues();
		values.put(ButtonConfigOpenHelper.BUTTONTYPE, button.getButtonType());
		values.put(ButtonConfigOpenHelper.DISPLAYAREA, button.getDisplayArea());
		values.put(ButtonConfigOpenHelper.GROUP, button.getGroup());
		values.put(ButtonConfigOpenHelper.POSITION, button.getPosition());
		values.put(ButtonConfigOpenHelper.TITLE, button.getTitle());
		values.put(ButtonConfigOpenHelper.ACTION, button.getAction());
		values.put(ButtonConfigOpenHelper.DRAWABLE, button.getDrawable());
		values.put(ButtonConfigOpenHelper.USESDRAWABLE, (button.getUsesDrawable() ? 1 : 0));
		values.put(ButtonConfigOpenHelper.EXTRADATA, button.getExtraData());

		return values;
	}
}

