package ca.efriesen.lydia.fragments;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.SMSConversation;
import ca.efriesen.lydia.databases.MessagesDataSource;
import ca.efriesen.lydia.includes.Helpers;
import ca.efriesen.lydia.includes.PhoneBaseAdapter;
import ca.efriesen.lydia.includes.SMSBaseAdapter;
import ca.efriesen.lydia_common.messages.PhoneCall;
import ca.efriesen.lydia_common.messages.SMS;
import ca.efriesen.lydia_common.includes.Intents;

/**
 * Created by eric on 2013-05-29.
 */
public class PhoneFragment extends Fragment {
	public static final String TAG = "phone";

	Activity activity;
	MessagesDataSource dataSource;
	ListView smslistView;
	ListView recentCallsView;

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		Activity activity = getActivity();
		// sms receiver listener
		activity.registerReceiver(smsReceiver, new IntentFilter(Intents.SMSRECEIVED));
		// phone call listener
		activity.registerReceiver(incomingCallReceiver, new IntentFilter(Intents.PHONECALL));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.phone_fragment, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		this.activity = getActivity();
		dataSource = new MessagesDataSource(getActivity());
		dataSource.open();

		smslistView = (ListView) activity.findViewById(R.id.sms_list);
		recentCallsView = (ListView) activity.findViewById(R.id.recent_calls);

		smslistView.setAdapter(new SMSBaseAdapter(activity.getApplicationContext(), dataSource.getAllSMS()));
		smslistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int positions, long id) {
				startActivity(new Intent(activity.getApplicationContext(), SMSConversation.class)
						.putExtra("message_id", id)
				);
			}
		});

		recentCallsView.setAdapter(new PhoneBaseAdapter(activity.getApplicationContext(), dataSource.getAllPhonecalls()));
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			activity.unregisterReceiver(smsReceiver);
		} catch (Exception e) {}
		try {
			activity.unregisterReceiver(incomingCallReceiver);
		} catch (Exception e) {}
	}

	private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final SMS sms = (SMS) intent.getSerializableExtra("ca.efriesen.SMS");
			final String phoneNumber = sms.getFromNumber();
			final String message = sms.getMessage();

			smslistView.setAdapter(new SMSBaseAdapter (activity.getApplicationContext(), dataSource.getAllSMS()));

			// store message in db
			dataSource.createMessage(message, phoneNumber, ca.efriesen.lydia.databases.Message.TYPE_SMS, false);

			final EditText reply = new EditText(activity);

			new AlertDialog.Builder(
					activity).setTitle(Helpers.getContactDisplayNameByNumber(activity, phoneNumber))
					.setMessage(message)
					.setView(reply)
					.setCancelable(false)
					.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							dialogInterface.cancel();
						}
					})
					.setPositiveButton(getString(R.string.reply), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							SMS smsReply = new SMS();
							smsReply.setMessage(reply.getText().toString());
							smsReply.setToNumber(phoneNumber);
							// pass the id into the reply from the original message
							smsReply.setId(sms.getId());

							activity.sendBroadcast(new Intent(Intents.SMSREPLY).putExtra("ca.efriesen.SMS", smsReply));
							dialogInterface.cancel();
						}
					}).create().show();
		}
	};

	public BroadcastReceiver incomingCallReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			PhoneCall call = (PhoneCall) intent.getSerializableExtra(Intents.PHONECALL);

			// store in db
			dataSource.createMessage("", call.getFromNumber(), ca.efriesen.lydia.databases.Message.TYPE_PHONE, false);
			recentCallsView.setAdapter(new PhoneBaseAdapter(activity.getApplicationContext(), dataSource.getAllPhonecalls()));

			new AlertDialog.Builder(activity).setTitle(getString(R.string.incoming_call))
					.setMessage(Helpers.getContactDisplayNameByNumber(activity, call.getFromNumber()))
					.setCancelable(false)
					.setPositiveButton(getText(R.string.answer), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							// answer call
							dialogInterface.cancel();
						}
					})
					.setNegativeButton(getText(R.string.ignore), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							// ignore call
							dialogInterface.cancel();
						}
					}).create().show();
		}
	};

}