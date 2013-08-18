package ca.efriesen.lydia.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.RFIDTag;
import ca.efriesen.lydia.databases.RFIDTagDataSource;
import ca.efriesen.lydia_common.includes.Intents;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by eric on 2013-08-18.
 */
public class RFIDSetup extends Activity {

	private RFIDTagDataSource dataSource;

	private RFIDTagViewAdapter storedTagsArrayAdapter;

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		setContentView(R.layout.rfid_setup);

		dataSource = new RFIDTagDataSource(this);
		dataSource.open();

		ArrayList<RFIDTag> tags = dataSource.getAllTags();
		storedTagsArrayAdapter = new RFIDTagViewAdapter(tags, this);

		// Find and set up the ListView for stored tags
		ListView storedTagsListView = (ListView) findViewById(R.id.stored_tags);

		storedTagsListView.setAdapter(storedTagsArrayAdapter);

		registerReceiver(tagFoundReceiver, new IntentFilter(Intents.RFID));

		if (tags.size() > 0) {
			(findViewById(R.id.no_tags_found)).setVisibility(View.GONE);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			unregisterReceiver(tagFoundReceiver);
		} catch (Exception e) {}
	}


	private BroadcastReceiver tagFoundReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();

			RFIDTag tag = new RFIDTag();
			tag.setTagNumber(intent.getLongExtra(Intents.RFID, 0));
			tag.setEnabled(false);
			tag.setDescription("New tag added " + dateFormat.format(date));

			dataSource.addTag(tag);
		}
	};

	// listview adapter for tags
	class RFIDTagViewAdapter extends BaseAdapter {
		private final List<RFIDTag> content;
		private final Activity activity;

		public RFIDTagViewAdapter(List<RFIDTag> content, Activity activity) {
			this.content = content;
			this.activity = activity;
		}

		public int getCount() {
			return content.size();
		}

		public RFIDTag getItem(int position) {
			return content.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView,	ViewGroup parent) {
			// inflate the view if not already done
			if (convertView == null) {
				LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.device_list_row, null);
			}

			// get the specific app we've pressed
			RFIDTag tag = content.get(position);
			if (tag != null) {
				// get the name view
				TextView name = (TextView) convertView.findViewById(R.id.device_name);
				TextView enabledText = (TextView) convertView.findViewById(R.id.enabled_text);

				convertView.findViewById(R.id.enabled).setTag(position);
				((CheckBox)convertView.findViewById(R.id.enabled)).setChecked(tag.getEnabled());
				enabledText.setText("Enabled");

				// set the name
				name.setText(tag.getDescription());
			}
			// return the view
			return convertView;
		}
	}

}
