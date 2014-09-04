package com.autosenseapp.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.*;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.*;
import android.widget.*;

import com.autosenseapp.GlobalClass;
import com.autosenseapp.R;
import com.autosenseapp.activities.Dashboard;
import com.autosenseapp.controllers.NotificationController;
import com.autosenseapp.fragments.NotificationFragments.MusicNotificationFragment;
import com.autosenseapp.services.MediaService;


/**
 * User: eric
 * Date: 2012-10-06
 * Time: 10:31 AM
 */
public class HeaderFragment extends Fragment { //implements View.OnTouchListener {

	public static final String TAG = HeaderFragment.class.getSimpleName();

	private Activity activity;
	private LocalBroadcastManager localBroadcastManager;
	private AudioManager mAudioManager;
	// var to store the volume for when the mute button is pressed
	private static int oldVolume;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.header_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		activity = getActivity();
		localBroadcastManager = LocalBroadcastManager.getInstance(activity);
	}

	@Override
	public void onStart() {
		super.onStart();

		// setup the audio manager from the main activity
		mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

		// get all the buttons
		ImageButton mute = (ImageButton) activity.findViewById(R.id.mute);

		ImageButton playPause = (ImageButton) activity.findViewById(R.id.play_pause);
		ImageButton previous = (ImageButton) activity.findViewById(R.id.previous);
		ImageButton next = (ImageButton) activity.findViewById(R.id.next);

		// mute button
		mute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get the current volume
				int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				// if it's not 0
				if (currentVolume > 0) {
					// mute the stream
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
					// store the volume
					oldVolume = currentVolume;
					// it's currently 0, so use the stored value
				} else {
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldVolume, 0);
				}
			}
		});

		playPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				localBroadcastManager.sendBroadcast(new Intent(MediaService.MEDIA_COMMAND).putExtra(MediaService.MEDIA_COMMAND, MediaService.PLAY_PAUSE));
			}
		});

		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				localBroadcastManager.sendBroadcast(new Intent(MediaService.MEDIA_COMMAND).putExtra(MediaService.MEDIA_COMMAND, MediaService.NEXT));
				// show the music bar on change
				((NotificationController)((GlobalClass)activity.getApplicationContext()).getController(GlobalClass.NOTIFICATION_CONTROLLER)).setNotification(MusicNotificationFragment.class);
			}
		});

		previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				localBroadcastManager.sendBroadcast(new Intent(MediaService.MEDIA_COMMAND).putExtra(MediaService.MEDIA_COMMAND, MediaService.PREVIOUS));
				((NotificationController)((GlobalClass)activity.getApplicationContext()).getController(GlobalClass.NOTIFICATION_CONTROLLER)).setNotification(MusicNotificationFragment.class);
			}
		});
	}
}