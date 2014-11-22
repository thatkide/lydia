package com.autosenseapp.services.media_states;

import android.content.Context;
import android.media.MediaPlayer;
import com.autosenseapp.controllers.MediaController;

/**
 * Created by eric on 1/3/2014.
 */
public class PlayState extends MediaState {

	private static final String TAG = "lydia playstate";
	private Context context;
	private MediaPlayer mediaPlayer;
	private MediaController mediaController;

	public PlayState(Context context, MediaController MediaController, MediaPlayer mediaPlayer) {
		super(context, MediaController, mediaPlayer);
		this.context = context;
		this.mediaPlayer = mediaPlayer;
		this.mediaController = MediaController;
	}

	@Override
	public void next() {
		super.next();
		mediaController.stop();
		mediaController.setState(mediaController.getPlayingState());
		mediaController.getState().setSong(mediaController.playlist.get(mediaController.playlistPosition));
	}

	@Override
	public void previous() {
		super.previous();
		mediaController.stop();
		mediaController.setState(mediaController.getPlayingState());
		mediaController.getState().setSong(mediaController.playlist.get(mediaController.playlistPosition));
	}

	@Override
	public void play() {
		mediaController.stop();
		mediaController.getState().setSong(mediaController.playlist.get(mediaController.playlistPosition));
	}

	@Override
	public void playPause() {
		mediaPlayer.pause();
		mediaController.setState(mediaController.getPausedState());
		// call super after we pause, then we will send the paused state
		super.playPause();
	}

}
