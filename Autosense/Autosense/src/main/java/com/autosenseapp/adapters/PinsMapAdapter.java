package com.autosenseapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by eric on 2014-09-02.
 */
// create new adapter for the pins map
public class PinsMapAdapter extends BaseAdapter {

	private Context context;
	private Map<Integer, Set<String>> pins;
	private Integer[] keys;
	private boolean analog = false;

	public PinsMapAdapter(Context context, Map<Integer, Set<String>> pins) {
		this.context = context;
		// copy the map
		this.pins = pins;
		// get the keys
		keys = pins.keySet().toArray(new Integer[pins.size()]);
		// sort the keys
		Arrays.sort(keys);
		// if we have negative numbers, sort inverse (we use negative for analog inputs)
		if (keys[0] < 0) {
			analog = true;
			Arrays.sort(keys, Collections.reverseOrder());
		}
	}

	public String getName(int position) {
		// additional names holder
		String nameString = "";

		if (!analog) {
			// get the pin number
			nameString += String.valueOf(getItemId(position));
		}
		// get any additional names
		Set<String> values = getItem(position);
		String additionalNames = " ";
		if (values != null) {
			// loop over them
			for (String name : values) {
				// append them
				additionalNames += name + " ";
			}
			if (!analog) {
				// add the parentheses
				nameString += " (" + additionalNames.trim() + ")";
			} else {
				nameString += additionalNames.trim();
			}
		}
		return nameString;
	}

	@Override
	public int getCount() {
		// return the number of elements
		if (pins != null) {
			return pins.size();
		} else {
			return 0;
		}
	}

	@Override
	public Set<String> getItem(int position) {
		// get the key from the pos passed
		// and return the value set
		return pins.get(getPinNumber(position));
	}

	@Override
	public long getItemId(int position) {
		return keys[position];
	}

	public int getPinNumber(int position) {
		return ((Long)getItemId(position)).intValue();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// setup our viewholder to recycle views
		ViewHolder viewHolder;

		// if convert view is null, we're starting fresh
		if (convertView == null) {
			// get the layout inflater
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
			// make a new holder
			viewHolder = new ViewHolder();
			// get the views
			viewHolder.pinNumber = (TextView) convertView.findViewById(android.R.id.text1);
			// set the tag of the viewholder to save from having to reinflate
			convertView.setTag(viewHolder);
		} else {
			// otherwise, just get the recycled view
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// set the text
		viewHolder.pinNumber.setText(getName(position));

		return convertView;
	}

	private class ViewHolder {
		TextView pinNumber;
	}

}