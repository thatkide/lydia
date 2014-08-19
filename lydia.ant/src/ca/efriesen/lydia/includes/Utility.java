package ca.efriesen.lydia.includes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

public class Utility {
	public static ArrayList<String> nameOfEvent = new ArrayList<String>();
	public static ArrayList<String> startDates = new ArrayList<String>();
	public static ArrayList<String> endDates = new ArrayList<String>();
	public static ArrayList<String> descriptions = new ArrayList<String>();

	public static ArrayList<String> readCalendarEvent(Context context) {
		Cursor cursor = context.getContentResolver()
				.query(Uri.parse("content://com.android.calendar/events"),
						new String[] { "calendar_id", "title", "description", "dtstart", "dtend", "eventLocation" },
						null,
						null,
						"dtstart");
		cursor.moveToFirst();
		// fetching calendars name
		String CNames[] = new String[cursor.getCount()];

		// fetching calendars id
		nameOfEvent.clear();
		startDates.clear();
		endDates.clear();
		descriptions.clear();
		for (int i = 0; i < CNames.length; i++) {
			String end = "0";
			try {
				end = getDate(Long.parseLong(cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DTEND))));
			} catch (Exception e) {}
			nameOfEvent.add(cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE)));
			startDates.add(getDate(Long.parseLong(cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DTSTART)))));
			endDates.add(end);
			descriptions.add(cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION)));
			CNames[i] = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION));
			cursor.moveToNext();

		}
		cursor.close();
		return nameOfEvent;
	}

	public static String getDate(long milliSeconds) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliSeconds);
		return formatter.format(calendar.getTime());
	}
}
