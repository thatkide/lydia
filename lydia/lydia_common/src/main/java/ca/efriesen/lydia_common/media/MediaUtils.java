package ca.efriesen.lydia_common.media;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by eric on 2013-06-23.
 */
public class MediaUtils {

	public static <T extends Media> ArrayList<T> cursorToArray(Class<T> c, Cursor cursor, Context context) {
		// make sure the cursor has info
		if (cursor.getCount() == 0) {
			return null;
		}

		cursor.moveToFirst();
		ArrayList<T> list = new ArrayList<T>();

		// loop over the cursor
		do {
			try {
				// get the constructor that takes a context
				Constructor constructor = c.getConstructor(Context.class);

				// create a new instance of the class passed
				T item = c.cast(constructor.newInstance(context));

				// get the setcursordata method from the class
				Method setCursorData = c.getMethod("setCursorData", Cursor.class);
				try {
					// invoke the method, passing the cursor
					setCursorData.invoke(item, cursor);
					// add the new item
					list.add(item);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		// keep going until the end
		} while (cursor.moveToNext());
		//return the list
		return list;
	}

	public static String convertMillis(long duration) {
		Date date = new Date(duration);
		DateFormat formatter;
		// if the duration is over an hour add the hour format
		if (duration > 3600000) {
			formatter = new SimpleDateFormat("HH:mm:ss");
			// if over 10 minutes, add the leading digit
		} else if (duration > 600000) {
			formatter = new SimpleDateFormat("mm:ss");
		} else {
			formatter = new SimpleDateFormat("m:ss");
		}
		return formatter.format(date);
	}

}
