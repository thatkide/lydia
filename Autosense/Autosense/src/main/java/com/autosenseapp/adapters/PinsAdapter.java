package com.autosenseapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.autosenseapp.R;
import com.autosenseapp.databases.ArduinoPin;
import java.util.List;

/**
 * Created by eric on 2014-09-13.
 */
public class PinsAdapter extends ArrayAdapter {
	private LayoutInflater inflater;

	public PinsAdapter(Context context, List<ArduinoPin> pinList) {
		super(context, android.R.layout.simple_list_item_2, pinList);

		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		ArduinoPin pin = (ArduinoPin) getItem(position);
		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.textView = (TextView) convertView.findViewById(android.R.id.text1);
			viewHolder.textView2 = (TextView) convertView.findViewById(android.R.id.text2);

			convertView.setTag(R.string.view_holder ,viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag(R.string.view_holder);
		}

		viewHolder.textView.setText(pin.toString());
		viewHolder.textView2.setText(pin.getComment());

		return convertView;
	}

	private static class ViewHolder {
		public TextView textView;
		public TextView textView2;
	}
}

