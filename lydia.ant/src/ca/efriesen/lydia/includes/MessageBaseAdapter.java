package ca.efriesen.lydia.includes;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public abstract class MessageBaseAdapter extends BaseAdapter {

	abstract public int getCount();

	abstract public Object getItem(int position);

	abstract public long getItemId(int position);

	public abstract View getView(int position, View convertView, ViewGroup parent);

	protected static class ViewHolder {
		TextView message;
		TextView phoneNumber;
		TextView time;
	}

	protected static String getDateFormat(String time) {
		DateFormat dateFormat;
		// messages from today
		if (Long.valueOf(time) > startOfDay()) {
			dateFormat = new SimpleDateFormat("HH:mm");
			// messages from before today
		} else {
			dateFormat = new SimpleDateFormat("LLL d");
		}
		dateFormat.setTimeZone(TimeZone.getDefault());

		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.setTimeInMillis(Long.valueOf(time));

		String result = dateFormat.format(calendar.getTime());
		calendar.clear();
		return result;
	}

	protected static long startOfDay() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
		cal.set(Calendar.MINUTE, 0); // set minutes to zero
		cal.set(Calendar.SECOND, 0); //set seconds to zero
		return cal.getTimeInMillis();
	}
}