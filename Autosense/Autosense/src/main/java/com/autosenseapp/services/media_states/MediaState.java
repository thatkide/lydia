package com.autosenseapp.services.media_states;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import com.autosenseapp.AutosenseApplication;
import com.autosenseapp.controllers.MediaController;
import ca.efriesen.lydia_common.media.Song;
import java.io.IOException;
import javax.inject.Inject;

/**
 * Created by eric on 1/3/2014.
 */
public abstract class MediaState {
	private static final String TAG = MediaState.class.getSimpleName();

	private Context context;
	private MediaController mediaController;
	private MediaPlayer mediaPlayer;

	public MediaState(Context context, MediaController MediaController, MediaPlayer mediaPlayer) {
		this.context = context;
		((AutosenseApplication)context.getApplicationContext()).inject(this);
		this.mediaPlayer = mediaPlayer;
		this.mediaController = MediaController;
	}

	public void next() {
		try {
			// move to next item in playlist
			if (mediaController.playlistPosition < mediaController.playlist.size()-1) {
				mediaController.playlistPosition++;
				// if we've reached the end
				// start at the beginning again (if repeat is on)
			} else {
				if (mediaController.repeatAll) {
					mediaController.playlistPosition = 0;
				}
			}
		} catch (NullPointerException e) { }
	}

	public void play() {};

	public void playPause() {};

	public void previous() {
		try {
			// move to next item in playlist
			if (mediaController.playlistPosition > 0) {
				mediaController.playlistPosition--;
				// if we've reached the end
				// start at the beginning again (if repeat is on)
			} else {
				if (mediaController.repeatAll) {
					mediaController.playlistPosition = mediaController.playlist.size()-1;
				}
			}
		} catch (NullPointerException e) { }
	};

	public void setSong(Song song) {
		Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getId());
		try {
			mediaPlayer.setDataSource(context, uri);
			mediaPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		} catch (NullPointerException e) {
		}
	}

	public void stop() {
		mediaController.mHandler.removeCallbacks(mediaController.mUpdateTime);
		mediaController.setState(mediaController.getStoppedState());
		mediaPlayer.stop();
		mediaPlayer.reset();
//		mediaPlayer.release();
	}
}
