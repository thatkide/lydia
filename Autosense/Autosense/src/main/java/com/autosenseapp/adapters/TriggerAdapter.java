package com.autosenseapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.autosenseapp.R;
import java.util.List;

/**
 * Created by eric on 2014-09-02.
 */
public class TriggerAdapter extends ArrayAdapter<String> {

	private Context context;
	private List<String> triggers;

	public TriggerAdapter(Context context, List<String> triggers) {
		super(context, R.layout.pin_trigger_row, triggers);
		this.context = context;
		this.triggers = triggers;
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
		viewHolder.textView.setText(triggers.get(position));

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
