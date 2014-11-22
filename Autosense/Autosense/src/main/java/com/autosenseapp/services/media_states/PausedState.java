package com.autosenseapp.services.media_states;

import android.content.Context;
import android.media.MediaPlayer;
import com.autosenseapp.controllers.MediaController;

/**
 * Created by eric on 1/3/2014.
 */
public class PausedState extends MediaState {

	private MediaPlayer mediaPlayer;
	private MediaController mediaController;

	public PausedState(Context context, MediaController mediaController, MediaPlayer mediaPlayer) {
		super(context, mediaController, mediaPlayer);
		this.mediaPlayer = mediaPlayer;
		this.mediaController = mediaController;
	}

	@Override
	public void next() {
		super.next();
		mediaController.stop();
		mediaController.setState(mediaController.getPausedState());
		// set the song.  this will check the state and not play in the paused state
		mediaController.getState().setSong(mediaController.playlist.get(mediaController.playlistPosition));
	}

	@Override
	public void play() {
		mediaController.stop();
		mediaController.getState().setSong(mediaController.playlist.get(mediaController.playlistPosition));
	}

	@Override
	public void previous() {
		super.previous();
		mediaController.stop();
		mediaController.setState(mediaController.getPausedState());
		mediaController.getState().setSong(mediaController.playlist.get(mediaController.playlistPosition));
	}

	@Override
	public void playPause() {
		mediaPlayer.start();
		mediaController.setState(mediaController.getPlayingState());
		// call super after we start, then we will send the playing state
		super.playPause();
	}
}
