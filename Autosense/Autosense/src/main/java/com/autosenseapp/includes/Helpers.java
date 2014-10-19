package com.autosenseapp.includes;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by eric on 2013-06-01.
 */
public class Helpers {

	public static void setupFullScreen(Window window) {
		// on android versions greater than or equal to kitkat, use immersive mode for the view.  this will truly make the app fullscreen
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			);
		}
	}

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
