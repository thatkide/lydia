package com.autosenseapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by eric on 2014-09-02.
 */
// create new adapter for the pins map
public class MapAdapter extends BaseAdapter {

	private Context context;
	private Map<String, String> values;
	private String[] keys;

	public MapAdapter(Context context, Map<String, String> values) {
		this.context = context;
		// copy the map
		this.values = values;
		// get the keys
		keys = values.keySet().toArray(new String[values.size()]);
	}

	@Override
	public int getCount() {
		return values.size();
	}

	@Override
	public String getItem(int position) {
		return values.get(keys[position]);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// setup our viewholder to recycle views
		ViewHolder viewHolder;

		// if convert view is null, we're starting fresh
		if (convertView == null) {
			// get the layout inflater
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
			// make a new holder
			viewHolder = new ViewHolder();
			// get the views
			viewHolder.value = (TextView) convertView.findViewById(android.R.id.text1);
			// set the tag of the viewholder to save from having to reinflate
			convertView.setTag(viewHolder);
		} else {
			// otherwise, just get the recycled view
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// set the text
		viewHolder.value.setText(getItem(position));

		return convertView;
	}

	private class ViewHolder {
		TextView value;
	}

}