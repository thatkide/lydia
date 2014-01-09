package ca.efriesen.lydia.services.media_states;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import ca.efriesen.lydia.services.MediaService;
import ca.efriesen.lydia_common.media.Song;

import java.io.IOException;

/**
 * Created by eric on 1/3/2014.
 */
public abstract class MediaState {
	private static final String TAG = "lydia mediastate";

	private Context context;
	private MediaService mediaService;
	private MediaPlayer mediaPlayer;

	public MediaState(Context context, MediaService mediaService, MediaPlayer mediaPlayer) {
		this.context = context;
		this.mediaPlayer = mediaPlayer;
		this.mediaService = mediaService;
	}

	public void next() {
		Log.d(TAG, "next " + getClass().getName());
		// move to next item in playlist
		if (mediaService.playlistPosition < mediaService.playlist.size()-1) {
			mediaService.playlistPosition++;
			// if we've reached the end
			// start at the beginning again (if repeat is on)
		} else {
			if (mediaService.repeatAll) {
				mediaService.playlistPosition = 0;
			}
		}
	}

	public void play() {};

	public void playPause() {
		Log.d(TAG, "playpause " + getClass().getName());
		mediaService.localBroadcastManager.sendBroadcast(new Intent(MediaService.IS_PLAYING).putExtra("isPlaying", mediaPlayer.isPlaying()));
	};

	public void previous() {
		Log.d(TAG, "previous " + getClass().getName());
		// move to next item in playlist
		if (mediaService.playlistPosition > 0) {
			mediaService.playlistPosition--;
			// if we've reached the end
			// start at the beginning again (if repeat is on)
		} else {
			if (mediaService.repeatAll) {
				mediaService.playlistPosition = mediaService.playlist.size()-1;
			}
		}
	};

	public void setSong(Song song) {
		Log.d(TAG, "setsong " + getClass().getName());
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
		Log.d(TAG, "stop " + getClass().getName());
		mediaService.mHandler.removeCallbacks(mediaService.mUpdateTime);
		mediaService.setState(mediaService.getStoppedState());
		mediaPlayer.stop();
		mediaPlayer.reset();
//		mediaPlayer.release();
	}
}
