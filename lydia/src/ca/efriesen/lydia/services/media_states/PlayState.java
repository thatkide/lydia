package ca.efriesen.lydia.services.media_states;

import android.content.Context;
import android.media.MediaPlayer;
import ca.efriesen.lydia.services.MediaService;

/**
 * Created by eric on 1/3/2014.
 */
public class PlayState extends MediaState {

	private Context context;
	private MediaPlayer mediaPlayer;
	private MediaService mediaService;

	public PlayState(Context context, MediaService mediaService, MediaPlayer mediaPlayer) {
		super(context, mediaService, mediaPlayer);
		this.context = context;
		this.mediaPlayer = mediaPlayer;
		this.mediaService = mediaService;
	}

	@Override
	public void next() {
		super.next();
		mediaService.stop();
		mediaService.setState(mediaService.getPlayingState());
		setSong(mediaService.playlist.get(mediaService.playlistPosition));
	}

	@Override
	public void previous() {
		super.previous();
		mediaService.stop();
		mediaService.setState(mediaService.getPlayingState());
		setSong(mediaService.playlist.get(mediaService.playlistPosition));
	}

	@Override
	public void play() {
		mediaService.stop();
		mediaService.getState().setSong(mediaService.playlist.get(mediaService.playlistPosition));
	}

	@Override
	public void playPause() {
		mediaPlayer.pause();
		mediaService.setState(mediaService.getPausedState());
		// call super after we pause, then we will send the paused state
		super.playPause();
	}

}
