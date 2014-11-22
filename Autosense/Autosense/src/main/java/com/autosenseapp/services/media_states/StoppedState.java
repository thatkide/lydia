package com.autosenseapp.services.media_states;

import android.content.Context;
import android.media.MediaPlayer;
import com.autosenseapp.AutosenseApplication;
import com.autosenseapp.controllers.MediaController;
import ca.efriesen.lydia_common.media.Album;
import ca.efriesen.lydia_common.media.Song;
import java.util.ArrayList;
import javax.inject.Inject;

/**
 * Created by eric on 1/3/2014.
 */
public class StoppedState extends MediaState {
	public static final String TAG = "lydia stopped state";

	@Inject Context context;
	private MediaController mediaController;

	public StoppedState(Context context, MediaController mediaService, MediaPlayer mediaPlayer) {
		super(context, mediaService, mediaPlayer);
		((AutosenseApplication)context.getApplicationContext()).inject(this);
		this.mediaController = mediaService;
	}

	@Override
	public void playPause() {
		ArrayList<Song> songs = Album.getAllSongs(context, mediaController.getShuffle());
		mediaController.setPlaylist(songs, 0);
		mediaController.getState().play();
		// do stuff first, then hit the super method
		super.playPause();
	}

	@Override
	public void play() {
		if (mediaController.playlist != null) {
			mediaController.setState(mediaController.getPlayingState());
			mediaController.getState().setSong(mediaController.playlist.get(mediaController.playlistPosition));
		}
	}

}
