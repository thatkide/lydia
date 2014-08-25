package com.autosenseapp.services.media_states;

import android.content.Context;
import android.media.MediaPlayer;

import com.autosenseapp.services.MediaService;
import ca.efriesen.lydia_common.media.Album;
import ca.efriesen.lydia_common.media.Song;

import java.util.ArrayList;

/**
 * Created by eric on 1/3/2014.
 */
public class StoppedState extends MediaState {
	public static final String TAG = "lydia stopped state";

	MediaPlayer mediaPlayer;
	MediaService mediaService;

	public StoppedState(Context context, MediaService mediaService, MediaPlayer mediaPlayer) {
		super(context, mediaService, mediaPlayer);
		this.mediaPlayer = mediaPlayer;
		this.mediaService = mediaService;
	}

	@Override
	public void playPause() {
		ArrayList<Song> songs = Album.getAllSongs(mediaService, mediaService.getShuffle());
		mediaService.setPlaylist(songs, 0);
		mediaService.getState().play();
		// do stuff first, then hit the super method
		super.playPause();
	}

	@Override
	public void play() {
		if (mediaService.playlist != null) {
			mediaService.setState(mediaService.getPlayingState());
			mediaService.getState().setSong(mediaService.playlist.get(mediaService.playlistPosition));
		}
	}

}
