package com.autosenseapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.autosenseapp.R;
import com.autosenseapp.databases.Message;
import com.autosenseapp.databases.MessagesDataSource;
import com.autosenseapp.includes.Helpers;
import com.autosenseapp.includes.SMSBaseAdapter;

import java.util.ArrayList;

/**
 * Created by eric on 2013-06-03.
 */
public class SMSConversation extends Activity {
	MessagesDataSource dataSource;
	ListView smslistView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutInflater().inflate(R.layout.sms_conversation, null));

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Long message_id = extras.getLong("message_id", 0);

			dataSource = new MessagesDataSource(getApplicationContext());
			dataSource.open();
			ArrayList<Message> messages = dataSource.getAllSMSById(message_id);

			TextView displayName = (TextView) findViewById(R.id.contact_display_name);
			displayName.setText(Helpers.getContactDisplayNameByNumber(getApplicationContext(), messages.get(0).getPhoneNumber()));

			smslistView = (ListView) findViewById(R.id.sms_conversation);
			smslistView.setAdapter(new SMSBaseAdapter(getApplicationContext(), messages));
			smslistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int positions, long id) {
				}
			});

		}
	}
}