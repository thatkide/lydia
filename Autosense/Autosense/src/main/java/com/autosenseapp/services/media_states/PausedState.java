package com.autosenseapp.services.media_states;

import android.content.Context;
import android.media.MediaPlayer;
import com.autosenseapp.services.MediaService;

/**
 * Created by eric on 1/3/2014.
 */
public class PausedState extends MediaState {

	private MediaPlayer mediaPlayer;
	private MediaService mediaService;

	public PausedState(Context context, MediaService mediaService, MediaPlayer mediaPlayer) {
		super(context, mediaService, mediaPlayer);
		this.mediaPlayer = mediaPlayer;
		this.mediaService = mediaService;
	}

	@Override
	public void next() {
		// run the super class of next
		super.next();
		// then stop the playback
		mediaService.stop();
		// set teh state to paused
		mediaService.setState(mediaService.getPausedState());
		// set the song.  this will check the state and not play in the paused state
		mediaService.getState().setSong(mediaService.playlist.get(mediaService.playlistPosition));
	}

	@Override
	public void play() {
		mediaService.stop();
		mediaService.getState().setSong(mediaService.playlist.get(mediaService.playlistPosition));
	}

	@Override
	public void previous() {
		// exact same as next, only previous song
		super.previous();
		mediaService.stop();
		mediaService.setState(mediaService.getPausedState());
		mediaService.getState().setSong(mediaService.playlist.get(mediaService.playlistPosition));
	}

	@Override
	public void playPause() {
		mediaPlayer.start();
		mediaService.setState(mediaService.getPlayingState());
		// call super after we start, then we will send the playing state
		super.playPause();
	}
}
