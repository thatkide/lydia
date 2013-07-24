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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		// hide ourself on create
		FragmentManager manager = getFragmentManager();
		manager.beginTransaction().hide(manager.findFragmentById(R.id.phone_fragment)).commit();

		return inflater.inflate(R.layout.phone_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		this.activity = getActivity();

		dataSource = new MessagesDataSource(getActivity());
		dataSource.open();

		smslistView = (ListView) activity.findViewById(R.id.sms_list);
		recentCallsView = (ListView) activity.findViewById(R.id.recent_calls);

		activity.registerReceiver(smsReceivedReceeiver, new IntentFilter(Intents.SMSRECEIVED));
		activity.registerReceiver(phoneCallReceivedReceiver, new IntentFilter(Intents.INCOMINGCALL));
		// sms receiver listener
		activity.registerReceiver(smsReceiver, new IntentFilter(Intents.SMSRECEIVED));
		// phone call listener
		activity.registerReceiver(incomingCallReceiver, new IntentFilter(Intents.INCOMINGCALL));
	}

	@Override
	public void onResume() {
		super.onResume();

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
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			activity.unregisterReceiver(smsReceivedReceeiver);
		} catch (Exception e) {
			Log.w(TAG, e);
		}
		try {
			activity.unregisterReceiver(phoneCallReceivedReceiver);
		} catch (Exception e) {
			Log.w(TAG, e);
		}
		try {
			activity.unregisterReceiver(smsReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			activity.unregisterReceiver(incomingCallReceiver);
		} catch (Exception e) {
			Log.w(TAG, e);
		}
	}

	public boolean onBackPressed() {
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.show(fragmentManager.findFragmentById(R.id.home_screen_container_fragment))
				.show(fragmentManager.findFragmentById(R.id.home_screen_fragment))
				.hide(fragmentManager.findFragmentById(R.id.phone_fragment))
				.addToBackStack(null)
				.commit();
		return true;
	}

	private BroadcastReceiver smsReceivedReceeiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			smslistView.setAdapter(new SMSBaseAdapter (activity.getApplicationContext(), dataSource.getAllSMS()));
		}
	};

	private BroadcastReceiver phoneCallReceivedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			recentCallsView.setAdapter(new PhoneBaseAdapter(activity.getApplicationContext(), dataSource.getAllPhonecalls()));
		}
	};

	private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			final String phoneNumber = intent.getStringExtra("phoneNumber");
			final String message = intent.getStringExtra("message");

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
//							Log.d(TAG, replyMessage);
							activity.sendBroadcast(new Intent(Intents.SMSREPLY).putExtra("message", reply.getText().toString()).putExtra("phoneNumber", phoneNumber));
							dialogInterface.cancel();
						}
					}).create().show();
		}
	};

	public BroadcastReceiver incomingCallReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			new AlertDialog.Builder(activity).setTitle(getString(R.string.incoming_call))
					.setMessage(Helpers.getContactDisplayNameByNumber(activity, intent.getStringExtra("number")))
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