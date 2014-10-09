package com.autosenseapp.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.autosenseapp.R;
import com.autosenseapp.activities.Dashboard;
import com.autosenseapp.controllers.ArduinoController;
import com.autosenseapp.controllers.MediaController;

import javax.inject.Inject;

/**
 * Created by eric on 2014-10-08.
 */
public class AutosenseService extends BaseService {

	private static final String TAG = AutosenseService.class.getSimpleName();

	@Inject ArduinoController arduinoController;
	@Inject MediaController mediaController;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// by having a notification we start the service in the foreground so it's less likely to be killed
		Notification.Builder builder = new Notification.Builder(this)
				.setSmallIcon(R.drawable.android)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.app_name));

		// a pending intent for the notification.  this will take us to the dashboard, or main activity
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, Dashboard.class), PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// Add a notification
		notificationManager.notify(4, builder.build());

		mediaController.onStart();
	}

	@Override
	public void onDestroy() {
		arduinoController.onDestroy();
	}
}
