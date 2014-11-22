package com.autosenseapp.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.autosenseapp.R;
import com.autosenseapp.activities.Dashboard;
import com.autosenseapp.controllers.ArduinoController;
import com.autosenseapp.controllers.MediaController;

import javax.inject.Inject;

import ca.efriesen.lydia_common.media.Song;

/**
 * Created by eric on 2014-10-08.
 */
public class AutosenseService extends BaseService {

	private static final String TAG = AutosenseService.class.getSimpleName();

	@Inject ArduinoController arduinoController;
	@Inject MediaController mediaController;
	@Inject LocalBroadcastManager localBroadcastManager;

	private Notification.Builder builder;
	private NotificationManager notificationManager;
	private int notificationId = 4;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// by having a notification we start the service in the foreground so it's less likely to be killed
		builder = new Notification.Builder(this)
				.setSmallIcon(R.drawable.android)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.app_name));

		// a pending intent for the notification.  this will take us to the dashboard, or main activity
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, Dashboard.class), PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// Add a notification
		notificationManager.notify(notificationId, builder.build());

		mediaController.onStart();
		localBroadcastManager.registerReceiver(currentPlayingMusicReceiver, new IntentFilter(MediaController.MEDIA_INFO));
	}

	@Override
	public void onDestroy() {
		arduinoController.onDestroy();
	}

	private BroadcastReceiver currentPlayingMusicReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Song currentSong = (Song) intent.getSerializableExtra(MediaController.SONG);
			if (currentSong.getIsPlaying()) {
				builder.setContentText(currentSong.getAlbum().getArtist().getName() + " - " + currentSong.getName());
				notificationManager.notify(notificationId, builder.build());
			} else {
				builder.setContentText(getString(R.string.app_name));
				notificationManager.notify(notificationId, builder.build());
			}
		}
	};
}
