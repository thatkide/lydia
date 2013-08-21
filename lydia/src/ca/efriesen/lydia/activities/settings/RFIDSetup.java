package ca.efriesen.lydia.activities.settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
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
import java.util.*;

/**
 * Created by eric on 2013-08-18.
 */
public class RFIDSetup extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener {

	private static final String TAG = "lydia rfid";
	private RFIDTagDataSource dataSource;

	private RFIDTagViewAdapter storedTagsArrayAdapter;
	private ArrayList<RFIDTag> tags;

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		setContentView(R.layout.rfid_setup);

		// open the db
		dataSource = new RFIDTagDataSource(this);
		dataSource.open();

		// get all the tags stored
		tags = dataSource.getAllTags();
		// setup the view adapter
		storedTagsArrayAdapter = new RFIDTagViewAdapter(tags, this);

		// Find and set up the ListView for stored tags
		ListView storedTagsListView = (ListView) findViewById(R.id.stored_tags);

		// set the adapter and click listener
		storedTagsListView.setAdapter(storedTagsArrayAdapter);
		storedTagsListView.setOnItemClickListener(this);
		storedTagsListView.setOnItemLongClickListener(this);

		// register a broadcast receiver to listen for new tags
		registerReceiver(tagFoundReceiver, new IntentFilter(Intents.RFID));

		// if the tags array is greater than 0, hide the "no tags found" text
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
			// make a new date format for the temp description
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();

			// create a new rfid tag
			RFIDTag tag = new RFIDTag();
			tag.setTagNumber(intent.getLongExtra(Intents.RFID, 0));
			tag.setEnabled(false);
			tag.setDescription("New tag added " + dateFormat.format(date));

			// try to add to the db, then check if successful, and if so, notify the view
			long id = dataSource.addTag(tag);
			// check the id returned, -1 indicates an error
			if (id != -1) {
				// set the id for the tag just created
				tag.setId(id);
				// add it to the array
				tags.add(tag);
				// notify that data was changed
				storedTagsArrayAdapter.notifyDataSetChanged();
			}
			// hide the no tags found text
			findViewById(R.id.no_tags_found).setVisibility(View.GONE);
		}
	};

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		RFIDTag tag = (RFIDTag) adapterView.getAdapter().getItem(position);
		Intent rfidTagConfig = new Intent(getApplicationContext(), RFIDTagConfig.class);
		rfidTagConfig.putExtra("rfid_tag", tag);
		rfidTagConfig.putExtra("list_id", position);
		startActivityForResult(rfidTagConfig, 1);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
		RFIDTag tag = (RFIDTag) adapterView.getAdapter().getItem(position);
		Log.d(TAG, "item clicked " + tag.getDescription() + " id " + tag.getId());

		// try to remove from the db, then check if successful, and if so, notify the view
		if (dataSource.removeTag(tag) != 0) {
			tags.remove(tag);
			storedTagsArrayAdapter.notifyDataSetChanged();
		}
		// check if the tag array is empty, and if so, show the "no tags found" view
		if (tags.size() == 0) {
			findViewById(R.id.no_tags_found).setVisibility(View.VISIBLE);
		}
		return true;
	}

	@Override
	public void onClick(View view) {
		CheckBox checkBox = (CheckBox) view;
		RFIDTag tag = tags.get((Integer)view.getTag());
		tag.setEnabled(checkBox.isChecked());
		dataSource.update(tag);
		tag.setEnabled(checkBox.isChecked());
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		try {
			// check the codes
			switch (requestCode) {
				case 1: {
					tags.remove(intent.getIntExtra("list_id", 0));
					tags.add((RFIDTag)intent.getSerializableExtra("rfid_tag"));

					Collections.sort(tags, new Comparator<RFIDTag>() {
						@Override
						public int compare(RFIDTag tag, RFIDTag tag2) {
							return tag.getDescription().compareToIgnoreCase(tag2.getDescription());
						}
					});
					storedTagsArrayAdapter.notifyDataSetChanged();
				}
			}
		} catch (Exception e) {}
	}


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

				convertView.findViewById(R.id.enabled).setOnClickListener(RFIDSetup.this);
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
