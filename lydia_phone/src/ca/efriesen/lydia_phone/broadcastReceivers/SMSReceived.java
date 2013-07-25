package ca.efriesen.lydia_phone.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import ca.efriesen.lydia_common.messages.SMS;

import java.util.ArrayList;

/**
 * User: eric
 * Date: 2013-07-24
 * Time: 11:32 AM
 */
public class SMSReceived extends BroadcastReceiver {
	private static final String TAG = "lydia smsreceived";
	public void onReceive(Context context, Intent intent) {
		SMS sms = (SMS) intent.getSerializableExtra("ca.efriesen.SMS");

		SmsManager smsManager = SmsManager.getDefault();
		try {
			ArrayList<String> msgStringArray = smsManager.divideMessage(sms.getMessage());
			// send the text via cell radio
			smsManager.sendMultipartTextMessage(sms.getToNumber(), null, msgStringArray, null, null);

			ContentValues values = new ContentValues();
			values.put("address", sms.getToNumber());
			values.put("body", sms.getMessage());
			// mark the reply as read
			values.put("read", true);
			// insert into the sms conversation send list
			context.getContentResolver().insert(Uri.parse("content://sms/sent"), values);

			// also mark the original as read
			values = new ContentValues();
			values.put("read", true);
			Log.d(TAG, "sms id " + sms.getId());
			context.getContentResolver().update(Uri.parse("content://sms/inbox"), values, "_id=" + sms.getId(), null);
		} catch (IllegalArgumentException e) {
			Log.d(TAG, "Text failed " + e);
		}
	}
}
