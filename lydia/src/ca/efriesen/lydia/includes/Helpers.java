package ca.efriesen.lydia.includes;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import ca.efriesen.lydia.R;

import java.util.ArrayList;

/**
 * Created by eric on 2013-06-01.
 */
public class Helpers {

	public static String getContactDisplayNameByNumber(Context context, String number) {
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		String name = "?";

		ContentResolver contentResolver = context.getContentResolver();
		Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

		try {
			if (contactLookup != null && contactLookup.getCount() > 0) {
				contactLookup.moveToNext();
				name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
				//String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}

		return name;
	}

	public static byte highByte(int number) {
		return (byte)(number >> 8);
	}

	public static byte lowByte(int number) {
		return (byte)number;
	}

	public static int word(int high, int low) {
		int value = high << 8;
		value += low;
		return value;
	}

	public static void swapArrayElements(ArrayList list, int first, int second) {
		Object temp = list.set(first, list.get(second));
		list.set(second, temp);
	}

	public static int map(float value, int fromLow, int fromHigh, int toLow, int toHigh) {
		float Y;
		Y = (value-fromLow)/(fromHigh-fromLow) * (toHigh-toLow) + toLow;
		return (int)Y;
	}
}
