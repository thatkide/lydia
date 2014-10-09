package com.autosenseapp.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import com.autosenseapp.R;
import com.autosenseapp.services.media_states.MediaState;
import com.autosenseapp.services.media_states.PausedState;
import com.autosenseapp.services.media_states.PlayState;
import com.autosenseapp.services.media_states.StoppedState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import javax.inject.Inject;
import javax.inject.Singleton;
import ca.efriesen.lydia_common.includes.Constants;
import ca.efriesen.lydia_common.media.Album;
import ca.efriesen.lydia_common.media.MediaUtils;
import ca.efriesen.lydia_common.media.Song;

/**
 * Created by eric on 2014-10-08.
 */
@Singleton
public class MediaController implements
		MediaPlayer.OnCompletionListener,
		MediaPlayer.OnErrorListener,
		MediaPlayer.OnPreparedListener,
		AudioManager.OnAudioFocusChangeListener {

	private static final String TAG = MediaController.class.getSimpleName();

	// Intent strings
	public static final String GET_CURRENT_SONG = "com.autosenseapp.lydia.MediaService.GetCurrentSong";
	public static final String IS_PLAYING = "com.autosenseapp.lydia.MediaService.IsPlaying";
	public static final String MEDIA_COMMAND = "com.autosenseapp.lydia.MediaService.MediaCommand";
	public static final String NEXT = "com.autosenseapp.lydia.MediaService.Next";
	public static final String PLAY = "com.autosenseapp.lydia.MediaService.Play";
	public static final String PLAY_PAUSE = "com.autosenseapp.lydia.MediaService.PlayPause";
	public static final String PREVIOUS = "com.autosenseapp.lydia.MediaService.Previous";
	public static final String PROGRESS = "com.autosenseapp.lydia.MediaService.Progress";
	public static final String REPEAT = "com.autosenseapp.lydia.MediaService.Repeat";
	public static final String REPEAT_STATE = "com.autosenseapp.lydia.MediaService.RepeatState";
	public static final String SET_PLAYLIST = "com.autosenseapp.lydia.MediaService.SetPlaylist";
	public static final String SET_POSITION = "com.autosenseapp.lydia.MediaService.SetPosition";
	public static final String SHUFFLE = "com.autosenseapp.lydia.MediaService.Shuffle";
	public static final String SHUFFLE_PLAY = "com.autosenseapp.lydia.MediaService.ShufflePlay";
	public static final String SHUFFLE_STATE = "com.autosenseapp.lydia.MediaService.ShuffleState";
	public static final String SONG = "com.autosenseapp.Song";
	public static final String SONG_FINISHED = "com.autosenseapp.lydia.MediaService.SongFinished";
	public static final String STOP = "com.autosenseapp.lydia.MediaService.Stop";
	public static final String UPDATE_MEDIA_INFO = "com.autosenseapp.lydia.MediaService.UpdateMediaInfo";

	@Inject AudioManager audioManager;
	@Inject Context context;
	private MediaPlayer mMediaPlayer;
	@Inject LocalBroadcastManager localBroadcastManager;
	@Inject SharedPreferences sharedPreferences;

	private MediaState pausedState;
	private MediaState playState;
	private MediaState stoppedState;
	private MediaState mediaState;

	// media player stuff
	public ArrayList<Song> playlist;
	public ArrayList<Song> playlistShuffled;
	public ArrayList<Song> playlistOrdered;
	public int playlistPosition = 0;
	public boolean repeatAll;
	public boolean shuffle;
	public Handler mHandler = new Handler();

	@Inject
	public MediaController() { }

	public void onStart() {
		// default state for repeat and shuffle
		repeatAll = sharedPreferences.getBoolean(Constants.REPEATALL, false);
		shuffle = sharedPreferences.getBoolean(Constants.SHUFFLE, false);

		localBroadcastManager.registerReceiver(getCurrentSongReceiver, new IntentFilter(GET_CURRENT_SONG));
		localBroadcastManager.registerReceiver(CommandReceiver, new IntentFilter(MEDIA_COMMAND));

		// setup the media player
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);

		// setup the states
		pausedState = new PausedState(context, this, mMediaPlayer);
		playState = new PlayState(context, this, mMediaPlayer);
		stoppedState = new StoppedState(context, this, mMediaPlayer);

		// default to the stopped state
		mediaState = stoppedState;

		// request audio focus
		int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		// if we aren't granted access
		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			// so stop everything
			// we're in the stopped state already, this will release all media player resources
			mediaState.stop();
		}

	}


	@Override
	public void onCompletion(MediaPlayer mp) {
		// some things (like last.fm) want a song finished broadcast
		// FIXME
		localBroadcastManager.sendBroadcast(new Intent(SONG_FINISHED).putExtra(SONG, playlist.get(playlistPosition)));
		mediaState.next();
	}

	@Override
	public boolean onError(MediaPlayer player, int what, int extra) {
		cleanUp();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {
		//Log.d(TAG, "onPrepared " + getClass().getName());
		// remove old callbacks
		mHandler.removeCallbacks(mUpdateTime);
		if (getState() != getPausedState()) {
			setState(getPlayingState());
			mediaPlayer.start();
		}
		// get the song to send to the notification and broadcast
		Song song = playlist.get(playlistPosition);

//		builder.setContentText(song.getAlbum().getArtist().getName() + " - " + song.getName());
//		notificationManager.notify(notificationId, builder.build());

		// set the duration in the song
		playlist.get(playlistPosition).setDuration(mediaPlayer.getDuration());
		playlist.get(playlistPosition).setDurationString(MediaUtils.convertMillis(mMediaPlayer.getDuration()));

		// send the new song as the update media info intent
		localBroadcastManager.sendBroadcast(new Intent(UPDATE_MEDIA_INFO).putExtra(IS_PLAYING, mMediaPlayer.isPlaying()).putExtra(SONG, song));
		mHandler.postDelayed(mUpdateTime, 25);

	}

	@Override
	public void onAudioFocusChange(int focusChange) {

	}

	public MediaState getPausedState() {
		return pausedState;
	}

	public MediaState getPlayingState() {
		return playState;
	}

	public ArrayList<Song> getPlaylist() {
		return playlist;
	}

	public MediaState getStoppedState() {
		return stoppedState;
	}

	private void cleanUp() {
		//Log.d(TAG, "cleanup");
		// stop sending broadcasts
		mHandler.removeCallbacks(mUpdateTime);

		// remove note text on cleanup
//		builder.setContentText("");
//		notificationManager.notify(notificationId, builder.build());
		// release media player resources
		try {
			mMediaPlayer.release();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		// reset media player completely
		try {
			mMediaPlayer.reset();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		// nullify the object
		mMediaPlayer = null;
		// declare it dead
		setState(getStoppedState());
	}

	public void play() {
		//Log.d(TAG, "play " + getClass().getName());
		mediaState.play();
	}

	public void stop() {
		//Log.d(TAG, "stop " + getClass().getName());
		mediaState.stop();
	}

	private void shufflePlay() {
		setShuffle(true);
		stop();
		ArrayList<Song> songs = Album.getAllSongs(context, getShuffle());
		setPlaylist(songs, 0);
		play();
	}

	public void setPlaylist(ArrayList<Song> playlist, int playlistStartPosition) {
		if (playlist.size() > 0) {
			//Log.d(TAG, "setplaylist " + getClass().getName());
			playlistOrdered = playlist;
			// copy the playlist into a new object.
			ArrayList<Song> shuffled = new ArrayList<Song>(playlist);
			// remove the item clicked
			shuffled.remove(playlistStartPosition);
			// shuffle what's left
			Collections.shuffle(shuffled);
			// make another new arraylist
			this.playlistShuffled = new ArrayList<Song>();
			// copy the song clicked into the first item
			this.playlistShuffled.add(playlist.get(playlistStartPosition));
			// add what's left
			this.playlistShuffled.addAll(shuffled);

			if (shuffle) {
				// start from the beginning
				this.playlistPosition = 0;
				this.playlist = playlistShuffled;
			} else {
				this.playlistPosition = playlistStartPosition;
				this.playlist = playlistOrdered;
			}
		}
	}

	public String getCurrentPositionString() {
		return MediaUtils.convertMillis(mMediaPlayer.getCurrentPosition());
	}

	public int getCurrentPosition() {
		return mMediaPlayer.getCurrentPosition();
	}

	public void setCurrentPosition(int position) {
		mMediaPlayer.seekTo(position);
	}

	public MediaState getState() {
		return mediaState;
	}

	public void setState(MediaState state) {
		//Log.d(TAG, "setState " + state.getClass().getName());
		mediaState = state;
	}

	public void playAll() {
		// run this in a new thread as not to block
		Thread shuffleThread = new Thread(new Runnable() {
			@Override
			public void run() {
				stop();
				// make a new album and name "all songs"
				Album album = new Album(context);
				album.setName(context.getString(R.string.all_songs));
				// get a list of all the songs
//				ArrayList<Song> allSongs = getAllSongsInAlbum(album);
				if (shuffle) {
					// set the start position to a random place in the list
					Random rand = new Random();
					// set the playlist
//					setPlaylist(allSongs, rand.nextInt(allSongs.size()));
				} else {
					// start at the beginning
//					setPlaylist(allSongs, 0);
				}
				// and play
//				play();
				// as soon as we're done, stop the thread
				Thread.currentThread().interrupt();
			}
		});
		shuffleThread.start();
	}

	public void toggleRepeat() {
		repeatAll = !repeatAll;
		sharedPreferences.edit().putBoolean(Constants.REPEATALL, repeatAll).commit();
		localBroadcastManager.sendBroadcast(new Intent(REPEAT_STATE).putExtra(REPEAT_STATE, repeatAll));
	}

	public boolean getShuffle() {
		return shuffle;
	}

	private void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
		sharedPreferences.edit().putBoolean(Constants.SHUFFLE, shuffle).commit();
		localBroadcastManager.sendBroadcast(new Intent(SHUFFLE_STATE).putExtra(SHUFFLE_STATE, shuffle));
	}

	public void toggleShuffle() {
		setShuffle(!shuffle);
		localBroadcastManager.sendBroadcast(new Intent(SHUFFLE_STATE).putExtra(SHUFFLE_STATE, shuffle));
		if (shuffle) {
			this.playlist = playlistShuffled;
		} else {
			try {
				// get the song playing from the shuffled playlist
				Song song = this.playlist.get(playlistPosition);
				// set the playlist back to the ordered one
				this.playlist = playlistOrdered;
				// set the position to the song in the unordered list
				playlistPosition = this.playlist.indexOf(song);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Runnable mUpdateTime = new Runnable() {
		@Override
		public void run() {
			localBroadcastManager.sendBroadcast(new Intent(PROGRESS).putExtra("currentPositionString", getCurrentPositionString()).putExtra("currentPositionInt" ,getCurrentPosition()));
			mHandler.postDelayed(mUpdateTime, 250);
		}
	};

	private BroadcastReceiver CommandReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String command = intent.getStringExtra(MEDIA_COMMAND);
			if (command.equals(NEXT)) {
				mediaState.next();
			} else if (command.equals(PREVIOUS)) {
				mediaState.previous();
			} else if (command.equals(PLAY)) {
				mediaState.play();
			} else if (command.equals(PLAY_PAUSE)) {
				mediaState.playPause();
			} else if (command.equals(REPEAT)) {
				toggleRepeat();
			} else if (command.equals(SET_PLAYLIST)) {
//				setPlaylist(intent.getIntegerArrayListExtra("playlist"), intent.getIntExtra("position", 0));
			} else if (command.equals(SET_POSITION)) {
				setCurrentPosition(intent.getIntExtra(SET_POSITION, 0));
			} else if (command.equals(SHUFFLE)) {
				toggleShuffle();
			} else if (command.equals(SHUFFLE_PLAY)) {
				shufflePlay();
			} else if (command.equals(STOP)) {
				stop();
			}
		}
	};

	private BroadcastReceiver getCurrentSongReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Song song = playlist.get(playlistPosition);
				// send the new song as the update media info intent
				localBroadcastManager.sendBroadcast(new Intent(UPDATE_MEDIA_INFO).putExtra(IS_PLAYING, mMediaPlayer.isPlaying()).putExtra(SONG, song));
			} catch (NullPointerException e) {}
		}
	};
}
