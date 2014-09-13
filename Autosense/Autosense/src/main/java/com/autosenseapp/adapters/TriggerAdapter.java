package com.autosenseapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.autosenseapp.R;
import com.autosenseapp.devices.triggers.Trigger;
import java.util.List;

/**
 * Created by eric on 2014-09-02.
 */
public class TriggerAdapter extends ArrayAdapter<Trigger> {

	private static final String TAG = TriggerAdapter.class.getSimpleName();

	private Context context;
	private LayoutInflater inflater;
	private List<Trigger> allTriggers;
	private List<Trigger> selectedTriggers;

	public TriggerAdapter(Context context, List<Trigger> allTriggers, List<Trigger> selectedTriggers) {
		super(context, android.R.layout.simple_list_item_1, allTriggers);
		this.context = context;
		this.allTriggers = allTriggers;
		this.selectedTriggers = selectedTriggers;

		inflater = LayoutInflater.from(context);
	}

	@Override
	public Trigger getItem(int position) {
		if (allTriggers.size() > 0) {
			return allTriggers.get(position);
		} else {
			return null;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		Trigger trigger = this.getItem(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.pin_trigger_row, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.textView = (TextView) convertView.findViewById(R.id.TextView);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.CheckBox);

			convertView.setTag(R.string.view_holder ,viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag(R.string.view_holder);
		}
		try {
			viewHolder.textView.setText(getItem(position).getName(context));
		} catch (Exception e) {}
		try {
			viewHolder.checkBox.setOnClickListener((View.OnClickListener) context);
		} catch (Exception e) {}

		viewHolder.checkBox.setTag(R.string.trigger, getItem(position));

		for (Trigger t : selectedTriggers) {
			// default checkbox to false
			viewHolder.checkBox.setChecked(false);
			if (trigger.getId() == t.getId()) {
				convertView.setTag(R.string.trigger, t);
				// if we've found a matching id, check it
				viewHolder.checkBox.setChecked(true);
				// and break the loop
				break;
			}
		}

		return convertView;
	}

	private static class ViewHolder {
		public TextView textView;
		public CheckBox checkBox;
	}
}
