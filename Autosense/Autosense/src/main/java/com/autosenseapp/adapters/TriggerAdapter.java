package com.autosenseapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.autosenseapp.R;
import java.util.Map;

/**
 * Created by eric on 2014-09-02.
 */
public class TriggerAdapter extends BaseAdapter {

	private Context context;
	private Map<String, String> triggers;
	private String[] keys;

	public TriggerAdapter(Context context, Map<String, String> triggers) {
		this.context = context;
		this.triggers = triggers;
		this.keys = triggers.keySet().toArray(new String[triggers.size()]);
	}

	@Override
	public int getCount() {
		return triggers.size();
	}

	@Override
	public String getItem(int position) {
		return triggers.get(keys[position]);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.pin_trigger_row, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.textView = (TextView) convertView.findViewById(R.id.TextView);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.CheckBox);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.textView.setText(getItem(position));
		try {
			viewHolder.checkBox.setOnClickListener((View.OnClickListener) context);
		} catch (Exception e) {}

		return convertView;
	}

	public static class ViewHolder {
		TextView textView;
		CheckBox checkBox;

		public CheckBox getCheckbox() {
			return checkBox;
		}
	}
}
