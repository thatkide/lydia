package ca.efriesen.lydia_phone.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import ca.efriesen.lydia_phone.services.BluetoothService;
import ca.efriesen.lydia_phone.services.ManagerService;

/**
 * User: eric
 * Date: 2012-10-28
 * Time: 2:14 PM
 */
public class StartAfterBoot extends BroadcastReceiver {
	public static final String TAG = "LydiaPhone";

	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Starting Manager Service");
		context.startService(new Intent(context, ManagerService.class));
	}
}
