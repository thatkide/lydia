package com.autosenseapp.fragments;

import android.content.*;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.*;
import android.widget.*;
import com.autosenseapp.R;
import com.autosenseapp.controllers.NotificationController;
import com.autosenseapp.fragments.NotificationFragments.MusicNotificationFragment;
import com.autosenseapp.services.MediaService;
import javax.inject.Inject;
import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * User: eric
 * Date: 2012-10-06
 * Time: 10:31 AM
 */
public class HeaderFragment extends BaseFragment { //implements View.OnTouchListener {

	public static final String TAG = HeaderFragment.class.getSimpleName();

	@Inject	LocalBroadcastManager localBroadcastManager;
	@Inject NotificationController notificationController;
	@Inject AudioManager audioManager;

	@InjectView(R.id.mute) ImageButton mute;
	@InjectView(R.id.play_pause) ImageButton playPause;
	@InjectView(R.id.previous) ImageButton previous;
	@InjectView(R.id.next) ImageButton next;


	// var to store the volume for when the mute button is pressed
	private static int oldVolume;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.header_fragment, container, false);
		ButterKnife.inject(this, view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
	}

	@Override
	public void onStart() {
		super.onStart();

		// mute button
		mute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get the current volume
				int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				// if it's not 0
				if (currentVolume > 0) {
					// mute the stream
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
					// store the volume
					oldVolume = currentVolume;
					// it's currently 0, so use the stored value
				} else {
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldVolume, 0);
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
				notificationController.setNotification(MusicNotificationFragment.class);
			}
		});

		previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				localBroadcastManager.sendBroadcast(new Intent(MediaService.MEDIA_COMMAND).putExtra(MediaService.MEDIA_COMMAND, MediaService.PREVIOUS));
				notificationController.setNotification(MusicNotificationFragment.class);
			}
		});
	}
}