package ca.efriesen.lydia.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric on 2013-06-02.
 */
public class MessagesDataSource {
	private SQLiteDatabase database;
	private MessageOpenHelper dbHelper;
	private String[] PROJECTION = {MessageOpenHelper.COLUMN_ID, MessageOpenHelper.MESSAGE, MessageOpenHelper.PHONENUMBER, MessageOpenHelper.TIMERECEIVED, MessageOpenHelper.TYPE, MessageOpenHelper.FROMME};

	private static final String TAG = "messages";

	public MessagesDataSource(Context context) {
		dbHelper = new MessageOpenHelper(context);
	}

	public void open() throws SQLiteException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Message createMessage(String message, String phone_number, String type, boolean from_me) {
		int fromMe = (from_me ? 1 : 0);

		ContentValues values = new ContentValues();
		values.put(MessageOpenHelper.MESSAGE, message);
		values.put(MessageOpenHelper.PHONENUMBER, phone_number);
		values.put(MessageOpenHelper.TYPE, type);
		values.put(MessageOpenHelper.TIMERECEIVED, System.currentTimeMillis());
		values.put(MessageOpenHelper.FROMME, fromMe);

		long insertId = database.insert(MessageOpenHelper.TABLE_NAME, null, values);

		Cursor cursor = database.query(MessageOpenHelper.TABLE_NAME, PROJECTION, MessageOpenHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
		cursor.moveToFirst();
		Message newMessage = cursorToMessage(cursor);
		cursor.close();
		return newMessage;
	}

	public void deleteMessage(Message message) {
		long id = message.getId();
		database.delete(MessageOpenHelper.TABLE_NAME, MessageOpenHelper.COLUMN_ID + " = " + id, null);
	}

	private ArrayList<Message> getAllMessages(String type, String phoneNumber) {
		ArrayList<Message> messages = new ArrayList<Message>();

		String SELECTION;
		String groupBy = null;
		String order = "DESC";

		if (phoneNumber == null) {
			groupBy = MessageOpenHelper.PHONENUMBER;
			SELECTION = MessageOpenHelper.TYPE + "=\"" + type + "\"";
		} else {
			SELECTION = MessageOpenHelper.TYPE + "=\"" + type + "\" AND " + MessageOpenHelper.PHONENUMBER + "=\"" + phoneNumber + "\"";
			order = "ASC";
		}

		Cursor cursor = database.query(
				MessageOpenHelper.TABLE_NAME,
				PROJECTION,
				SELECTION,
				null,
				groupBy,
				null,
				MessageOpenHelper.TIMERECEIVED + " " + order
		);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Message message = cursorToMessage(cursor);
			messages.add(message);
			cursor.moveToNext();
		}

		cursor.close();
		return messages;
	}

	public ArrayList<Message> getAllPhonecalls() {
		return getAllMessages(Message.TYPE_PHONE, null);
	}

	public ArrayList<Message> getAllSMSById(Long id) {
		Cursor cursor = database.query(
				MessageOpenHelper.TABLE_NAME,
				new String[] {MessageOpenHelper.COLUMN_ID, MessageOpenHelper.PHONENUMBER},
				MessageOpenHelper.COLUMN_ID + "=" + id,
				null, null, null, null
		);
		cursor.moveToFirst();
		String phoneNumber = cursor.getString(cursor.getColumnIndex(MessageOpenHelper.PHONENUMBER));
		cursor.close();
		return getAllMessages(Message.TYPE_SMS, phoneNumber);
	}

	public ArrayList<Message> getAllSMS() {
		return getAllMessages(Message.TYPE_SMS, null);
	}

	private Message cursorToMessage(Cursor cursor) {
		Message message = new Message();
		message.setId(cursor.getLong(cursor.getColumnIndex(MessageOpenHelper.COLUMN_ID)));
		message.setMessage(cursor.getString(cursor.getColumnIndex(MessageOpenHelper.MESSAGE)));
		message.setPhoneNumber(cursor.getString(cursor.getColumnIndex(MessageOpenHelper.PHONENUMBER)));
		message.setTimeReceived(cursor.getLong(cursor.getColumnIndex(MessageOpenHelper.TIMERECEIVED)));
		message.setType(cursor.getString(cursor.getColumnIndex(MessageOpenHelper.TYPE)));
		message.setFromMe(cursor.getInt(cursor.getColumnIndex(MessageOpenHelper.FROMME))>0);

		return message;
	}
}
