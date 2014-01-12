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
		try {
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
		} catch (NullPointerException e) { }
	}

	public void play() {};

	public void playPause() {
		mediaService.localBroadcastManager.sendBroadcast(new Intent(MediaService.UPDATE_MEDIA_INFO).putExtra(MediaService.IS_PLAYING, mediaPlayer.isPlaying()).putExtra(MediaService.SONG, mediaService.playlist.get(mediaService.playlistPosition)));
	};

	public void previous() {
		try {
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
		mediaService.mHandler.removeCallbacks(mediaService.mUpdateTime);
		mediaService.setState(mediaService.getStoppedState());
		mediaPlayer.stop();
		mediaPlayer.reset();
//		mediaPlayer.release();
	}
}
