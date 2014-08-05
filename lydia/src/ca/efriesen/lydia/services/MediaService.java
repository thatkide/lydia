package ca.efriesen.lydia.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.Dashboard;
import ca.efriesen.lydia.services.media_states.MediaState;
import ca.efriesen.lydia.services.media_states.PausedState;
import ca.efriesen.lydia.services.media_states.PlayState;
import ca.efriesen.lydia.services.media_states.StoppedState;
import ca.efriesen.lydia_common.includes.Constants;
import ca.efriesen.lydia_common.media.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by eric on 2013-06-16.
 */
public class MediaService extends Service implements
		MediaPlayer.OnCompletionListener,
		MediaPlayer.OnErrorListener,
		MediaPlayer.OnPreparedListener,
		AudioManager.OnAudioFocusChangeListener {

	// other services
	AudioManager audioManager = null;
	MediaPlayer mMediaPlayer = null;

	// Intent strings
	public static final String GET_CURRENT_SONG = "ca.efriesen.lydia.MediaService.GetCurrentSong";
	public static final String IS_PLAYING = "ca.efriesen.lydia.MediaService.IsPlaying";
	public static final String MEDIA_COMMAND = "ca.efriesen.lydia.MediaService.MediaCommand";
	public static final String NEXT = "ca.efriesen.lydia.MediaService.Next";
	public static final String PLAY = "ca.efriesen.lydia.MediaService.Play";
	public static final String PLAY_PAUSE = "ca.efriesen.lydia.MediaService.PlayPause";
	public static final String PREVIOUS = "ca.efriesen.lydia.MediaService.Previous";
	public static final String PROGRESS = "ca.efriesen.lydia.MediaService.Progress";
	public static final String REPEAT = "ca.efriesen.lydia.MediaService.Repeat";
	public static final String REPEAT_STATE = "ca.efriesen.lydia.MediaService.RepeatState";
	public static final String SET_PLAYLIST = "ca.efriesen.lydia.MediaService.SetPlaylist";
	public static final String SET_POSITION = "ca.efriesen.lydia.MediaService.SetPosition";
	public static final String SHUFFLE = "ca.efriesen.lydia.MediaService.Shuffle";
	public static final String SHUFFLE_PLAY = "ca.efriesen.lydia.MediaService.ShufflePlay";
	public static final String SHUFFLE_STATE = "ca.efriesen.lydia.MediaService.ShuffleState";
	public static final String SONG = "ca.efriesen.Song";
	public static final String SONG_FINISHED = "ca.efriesen.lydia.MediaService.SongFinished";
	public static final String STOP = "ca.efriesen.lydia.MediaService.Stop";
	public static final String UPDATE_MEDIA_INFO = "ca.efriesen.lydia.MediaService.UpdateMediaInfo";

	// service bindings
	private final IBinder mBinder = new MediaServiceBinder();

	// notification vars
	private Notification.Builder builder;
	private NotificationManager notificationManager;
	private int notificationId = 12;

	public static final String TAG = "lydia mediaService";

	// other stuff
	private SharedPreferences sharedPreferences;

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

	public LocalBroadcastManager localBroadcastManager;

	public class MediaServiceBinder extends Binder {
		public MediaService getService() {
			return MediaService.this;
		}
	}

	// Binding method
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		sharedPreferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		localBroadcastManager = LocalBroadcastManager.getInstance(this);

		// start it in the foreground so it doesn't get killed
		builder = new Notification.Builder(this)
				.setSmallIcon(R.drawable.av_play)
				.setContentTitle("Music")
				.setOnlyAlertOnce(true)
				.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, Dashboard.class), PendingIntent.FLAG_UPDATE_CURRENT));

		startForeground(notificationId, builder.build());

		// Add a notification
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(notificationId, builder.build());

		// default state for repeat and shuffle
		repeatAll = sharedPreferences.getBoolean(Constants.REPEATALL, false);
		shuffle = sharedPreferences.getBoolean(Constants.SHUFFLE, false);

		localBroadcastManager.registerReceiver(getCurrentSongReceiver, new IntentFilter(GET_CURRENT_SONG));
		localBroadcastManager.registerReceiver(CommandReceiver, new IntentFilter(MEDIA_COMMAND));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			localBroadcastManager.unregisterReceiver(CommandReceiver);
		} catch (Exception e) {}
		try {
			localBroadcastManager.unregisterReceiver(getCurrentSongReceiver);
		} catch (Exception e) {}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// setup the media player
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

		// setup the states
		pausedState = new PausedState(getApplicationContext(), this, mMediaPlayer);
		playState = new PlayState(getApplicationContext(), this, mMediaPlayer);
		stoppedState = new StoppedState(getApplicationContext(), this, mMediaPlayer);

		// default to the stopped state
		mediaState = stoppedState;

		// get the audio manager
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// request audio focus
		int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		// if we aren't granted access
		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			// so stop everything
			// we're in the stopped state already, this will release all media player resources
			mediaState.stop();
		}

		return START_NOT_STICKY;
	}

	// Media player specific methods

	public void onAudioFocusChange(int focusChange) {
//		switch (focusChange) {
//			case AudioManager.AUDIOFOCUS_GAIN: {
////				if (getState() == State.Playing) {
//					// resume playback
//					if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
//						mMediaPlayer.start();
//					}
//					mMediaPlayer.setVolume(1.0f, 1.0f);
////				}
//				break;
//			}
//			case AudioManager.AUDIOFOCUS_LOSS: {
//				// we've lost focus, so stop
//				stop();
//				cleanUp();
//				break;
//			}
//			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
//				// lost focus for a short time, so we have to stop playback
//				// we call pause directly to keep the state "playing"
//				mMediaPlayer.pause();
//				break;
//			}
//			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
//				// lost focus for a short time, but we can duck
//				if (mMediaPlayer.isPlaying()) {
//					mMediaPlayer.setVolume(0.1f, 0.1f);
//				}
//				break;
//			}
//		}
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

		builder.setContentText(song.getAlbum().getArtist().getName() + " - " + song.getName());
		notificationManager.notify(notificationId, builder.build());

		// set the duration in the song
		playlist.get(playlistPosition).setDuration(mediaPlayer.getDuration());
		playlist.get(playlistPosition).setDurationString(MediaUtils.convertMillis(mMediaPlayer.getDuration()));

		// send the new song as the update media info intent
		localBroadcastManager.sendBroadcast(new Intent(UPDATE_MEDIA_INFO).putExtra(IS_PLAYING, mMediaPlayer.isPlaying()).putExtra(SONG, song));
		mHandler.postDelayed(mUpdateTime, 25);
	}

	// get state helper methods

	public MediaState getPausedState() {
		return pausedState;
	}

	public MediaState getPlayingState() {
		return playState;
	}

	public MediaState getStoppedState() {
		return stoppedState;
	}

	private void cleanUp() {
		//Log.d(TAG, "cleanup");
		// stop sending broadcasts
		mHandler.removeCallbacks(mUpdateTime);

		// remove note text on cleanup
		builder.setContentText("");
		notificationManager.notify(notificationId, builder.build());
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
		ArrayList<Song> songs = Album.getAllSongs(this, getShuffle());
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
				Album album = new Album(getApplicationContext());
				album.setName(getString(R.string.all_songs));
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
