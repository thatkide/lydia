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
	public static final String SONG = "com.autosenseapp.Song";
	public static final String SONG_FINISHED = "com.autosenseapp.lydia.MediaService.SongFinished";
	public static final String MEDIA_INFO = "com.autosenseapp.MediaService.MediaInfo";
	public static final String MEDIA_NEXT = "com.autosenseapp.com.MediaService.Next";
	public static final String MEDIA_PLAY = "com.autosenseapp.com.MediaService.Play";
	public static final String MEDIA_PLAYPAUSE = "com.autosenseapp.com.MediaService.PlayPause";
	public static final String MEDIA_PREV = "com.autosenseapp.com.MediaService.Previous";
	public static final String MEDIA_STOP = "com.autosenseapp.com.MediaService.Stop";

	@Inject AudioManager audioManager;
	@Inject Context context;
	private MediaPlayer mediaPlayer;
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

		// setup the media player
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);

		// setup the states
		pausedState = new PausedState(context, this, mediaPlayer);
		playState = new PlayState(context, this, mediaPlayer);
		stoppedState = new StoppedState(context, this, mediaPlayer);

		// default to the stopped state
		mediaState = stoppedState;

		int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		// if we aren't granted access
		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			// we're in the stopped state already, this will release all media player resources
			mediaState.stop();
		}

		try {
			context.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					next();
				}
			}, new IntentFilter(MEDIA_NEXT));
		} catch (Exception e) {}
		try {
			context.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					play();
				}
			}, new IntentFilter(MEDIA_PLAY));
		} catch (Exception e) {}
		try {
			context.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					playPause();
				}
			}, new IntentFilter(MEDIA_PLAYPAUSE));
		} catch (Exception e) {}
		try {
			context.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					previous();
				}
			}, new IntentFilter(MEDIA_PREV));
		} catch (Exception e) {}
		try {
			context.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					stop();
				}
			}, new IntentFilter(MEDIA_STOP));
		} catch (Exception e) {}

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
		Song currentSong = playlist.get(playlistPosition);

		// set the duration in the song
		currentSong.setDuration(mediaPlayer.getDuration());
		currentSong.setDurationString(MediaUtils.convertMillisToMinutesAndSeconds(this.mediaPlayer.getDuration()));

		// send the new song as the update media info intent
//		localBroadcastManager.sendBroadcast(new Intent(UPDATE_MEDIA_INFO).putExtra(IS_PLAYING, mediaPlayer.isPlaying()).putExtra(SONG, currentSong));
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

	public MediaState getStoppedState() {
		return stoppedState;
	}

	public MediaState getState() {
		return mediaState;
	}

	public void setState(MediaState state) {
		mediaState = state;
	}

	public ArrayList<Song> getPlaylist() {
		return playlist;
	}

	private void cleanUp() {
		// stop sending broadcasts
		mHandler.removeCallbacks(mUpdateTime);

		// release media player resources
		try {
			mediaPlayer.release();
		} catch (Exception e) { }
		// reset media player completely
		try {
			mediaPlayer.reset();
		} catch (Exception e) { }
		// nullify the object
		mediaPlayer = null;
		// declare it dead
		setState(getStoppedState());
	}

	public void playPause() {
		mediaState.playPause();
	}

	public void play() {
		getCurrentSong().setIsPlaying(mediaPlayer.isPlaying());
		mediaState.play();
	}

	public void stop() {
		getCurrentSong().setIsPlaying(mediaPlayer.isPlaying());
		mediaState.stop();
	}

	public void next() {
		mediaState.next();
	}

	public void previous() {
		mediaState.previous();
	}

	public void shufflePlay() {
		setShuffle(true);
		stop();
		ArrayList<Song> songs = Album.getAllSongs(context, getShuffle());
		setPlaylist(songs, 0);
		play();
	}

	public void setPlaylist(ArrayList<Song> playlist, int playlistStartPosition) {
		if (playlist.size() > 0) {
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

	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	public void setCurrentPosition(int position) {
		mediaPlayer.seekTo(position);
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
		sharedPreferences.edit().putBoolean(Constants.REPEATALL, repeatAll).apply();
	}

	public boolean getRepeat() {
		return repeatAll;
	}

	public boolean getShuffle() {
		return shuffle;
	}

	private void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
		sharedPreferences.edit().putBoolean(Constants.SHUFFLE, shuffle).apply();
	}

	public void toggleShuffle() {
		setShuffle(!shuffle);
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

	public Song getCurrentSong() {
		if (playlist != null) {
			return playlist.get(playlistPosition);
		} else {
			return null;
		}
	}

	public Runnable mUpdateTime = new Runnable() {
		@Override
		public void run() {
			Song currentSong = getCurrentSong();
			currentSong.setCurrentPosition(getCurrentPosition());
			currentSong.setIsPlaying(mediaPlayer.isPlaying());

			localBroadcastManager.sendBroadcast(new Intent(MEDIA_INFO).putExtra(SONG, currentSong));
			mHandler.postDelayed(mUpdateTime, 250);
		}
	};
}