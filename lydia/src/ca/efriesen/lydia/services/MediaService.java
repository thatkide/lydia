package ca.efriesen.lydia.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.Dashboard;
import ca.efriesen.lydia_common.includes.Constants;
import ca.efriesen.lydia_common.includes.Intents;
import ca.efriesen.lydia_common.media.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 * Created by eric on 2013-06-16.
 */
public class MediaService extends Service implements
		MediaPlayer.OnPreparedListener,
		MediaPlayer.OnErrorListener,
		AudioManager.OnAudioFocusChangeListener {

	// other services
	AudioManager audioManager = null;
	MediaPlayer mMediaPlayer = null;

	// service bindings
	private final IBinder mBinder = new MediaServiceBinder();

	// notification vars
	private Notification.Builder builder;
	private NotificationManager notificationManager;
	private int notificationId = 12;

	public static final String TAG = "lydia Media Service V2";

	// database stuff
	private Uri mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

	// other stuff
	SharedPreferences sharedPreferences;

	// inidcate the state of the service
	public enum State {
		// user modes are "stopped (paused)" and "playing"
		Dead,		// nothing is ready
		Playing,	// playback active (media player ready!)
		Paused,		// playback paused (media player ready!)
		Stopped,	// we call user pause stopped, and program pause pause
	}

	private State mState = State.Dead;

	// media player stuff
	private ArrayList<Song> playlist;
	private ArrayList<Song> playlistShuffled;
	private ArrayList<Song> playlistOrdered;
	private int playlistPosition;
	private boolean repeatAll;
	private boolean shuffle;

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

	// Service methods

	@Override
	public void onCreate() {
		super.onCreate();
		sharedPreferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);

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

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// init
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			// we can't get focus, so cleanup
			cleanUp();
		}
		initMediaPlayer();
		return START_NOT_STICKY;
	}

	// Media player specific methods

	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN: {
				if (getState() == State.Playing) {
					// resume playback
					if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
						mMediaPlayer.start();
					}
					mMediaPlayer.setVolume(1.0f, 1.0f);
				}
				break;
			}
			case AudioManager.AUDIOFOCUS_LOSS: {
				// we've lost focus, so stop
				stop();
				cleanUp();
				break;
			}
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
				// lost focus for a short time, so we have to stop playback
				// we call pause directly to keep the state "playing"
				mMediaPlayer.pause();
				break;
			}
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
				// lost focus for a short time, but we can duck
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.setVolume(0.1f, 0.1f);
				}
				break;
			}
		}
	}

	@Override
	public boolean onError(MediaPlayer player, int what, int extra) {
		cleanUp();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {
		mMediaPlayer.start();
		setState(State.Playing);

		Song song = (Song) playlist.get(playlistPosition);

		builder.setContentText(song.getAlbum().getArtist().getName() + " - " + song.getName());
		notificationManager.notify(notificationId, builder.build());

		// set the duration in the song
		playlist.get(playlistPosition).setDuration(mediaPlayer.getDuration());
		playlist.get(playlistPosition).setDurationString(MediaUtils.convertMillis(mMediaPlayer.getDuration()));

		// send the new song as the update media info intent
		sendBroadcast(new Intent(Intents.UPDATEMEDIAINFO).putExtra("ca.efriesen.Song", song));
	}

	synchronized private void initMediaPlayer() {
		// make sure we start dead, the callback will set us to prepared
		setState(State.Stopped);

		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		// the method called when a song finishes
		mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				Intent songFinished = new Intent(Intents.SONGFINISHED);
				songFinished.putExtra("ca.efriesen.Song", playlist.get(playlistPosition));
				sendBroadcast(songFinished);
				nextSong();
			}
		});
	}

	private void cleanUp() {
		// remove note text on cleanup
		builder.setContentText("");
		notificationManager.notify(notificationId, builder.build());
		// release media player resources
		try {
			mMediaPlayer.release();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		catch (IllegalStateException e) {
			e.printStackTrace();
		}
		// reset media player completely
		try {
			mMediaPlayer.reset();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		catch (IllegalStateException e) {
			e.printStackTrace();
		}
		// nullify the object
		mMediaPlayer = null;
		// declare it dead
		setState(State.Dead);
	}

	synchronized private void setSong(Song song) {
		Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getId());
		try {
			mMediaPlayer.setDataSource(getApplicationContext(), uri);
			mMediaPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		} catch (NullPointerException e) {
		}
	}

	public void previousSong() {
		stop();
		if (playlist != null) {
			// move to the previous item in the playlist
			if (playlistPosition > 0) {
				playlistPosition--;
			// if we've reached the beginning
			// go to the end (if repeat is on)
			} else {
				if (repeatAll) {
					playlistPosition = playlist.size()-1;
				} else {
					cleanUp();
					return;
				}
			}
			play();
		}
	}

	public void nextSong() {
		stop();
		if (playlist != null) {
			// move to next item in playlist
			if (playlistPosition < playlist.size()-1) {
				playlistPosition++;
			// if we've reached the end
			// start at the beginning again (if repeat is on)
			} else {
				if (repeatAll) {
					playlistPosition = 0;
				} else {
					cleanUp();
					return;
				}
			}
			play();
		}
	}

	synchronized public void stop() {
		if (getState() == State.Playing) {
			mMediaPlayer.reset();
			setState(State.Stopped);
		}
	}

	public void pause() {
		setState(State.Paused);
		mMediaPlayer.pause();
	}

	synchronized public void play() {
		if (getState() == State.Dead) {
			initMediaPlayer();
			// if we have an empty playlist
			if (playlist == null) {
				// play all.  this will start a new thread that queries the db and starts playing when ready
				playAll();
				setState(State.Playing);
				return;
			}
		}
		if (getState() == State.Paused) {
			mMediaPlayer.start();
		} else {
			setSong(playlist.get(playlistPosition));
		}
		setState(State.Playing);
	}

	synchronized public void setPlaylist(ArrayList<Song> playlist, int playlistStartPosition) {
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

		if (getShuffle()) {
			// start from the beginning
			this.playlistPosition = 0;
			this.playlist = playlistShuffled;
		} else {
			this.playlistPosition = playlistStartPosition;
			this.playlist = playlistOrdered;
		}
	}

	public ArrayList<Media> getAllLike(Class clazz, String search) {
		String[] PROJECTION = new String[] {
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ARTIST_ID,
				MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.ALBUM_ID,
				MediaStore.Audio.Media.TRACK,
		};

		String ORDER = MediaStore.Audio.Media.TITLE + " asc limit 100";
		String SELECTION;
		if (clazz.getClass().equals(Artist.class)) {
			SELECTION = MediaStore.Audio.Media.ARTIST + " LIKE '%" + search + "%'";
		} else if (clazz.getClass().equals(Album.class)) {
			SELECTION = MediaStore.Audio.Media.ALBUM + " LIKE '%" + search + "%'";
		} else {
			SELECTION = MediaStore.Audio.Media.TITLE + " LIKE '%" + search + "%'";
		}

		Cursor cursor = getContentResolver().query(mediaUri, PROJECTION, SELECTION, null, ORDER);

		// if we found 1 item or more
		if (cursor.getCount() > 0) {
			// make a new array list
			ArrayList<Media> medias = MediaUtils.cursorToArray(Media.class, cursor);
			// close the db
			cursor.close();
			// and return
			return medias;
		// we didn't find anything
		} else {
			// close the db
			cursor.close();
			// make a new list
			ArrayList<Media> mediaList = new ArrayList<Media>();
			// make a new media object
			Media media = new Media();
			media.setId(-1);
			media.setName(getString(R.string.nothing_found));
			// add it to the list
			mediaList.add(media);
			// and return
			return mediaList;
		}
	}

	public ArrayList<Artist> getAllArtists() {
		String[] PROJECTION = new String[] {
				"DISTINCT " + MediaStore.Audio.Media.ARTIST_ID + " AS " + MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ARTIST_ID,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ARTIST_KEY,
				MediaStore.Audio.Media.IS_MUSIC
		};
		String ORDER = MediaStore.Audio.Media.ARTIST + " COLLATE NOCASE ASC";
		String SELECTION = MediaStore.Audio.Media.IS_MUSIC + " > 0";
		Cursor cursor = getContentResolver().query(mediaUri, PROJECTION, SELECTION, null, ORDER);

		ArrayList<Artist> artists = MediaUtils.cursorToArray(Artist.class, cursor);
		cursor.close();

		try {
			// since the db won't ignore "the" when sorting, we set the artist name to move "the" to the end, and then resort
			Collections.sort(artists, new Comparator<Artist>() {
				@Override
				public int compare(Artist artist, Artist artist2) {
					return artist.getSortName().compareToIgnoreCase(artist2.getSortName());
				}
			});
		} catch (NullPointerException e) {
			Log.e(TAG, e.toString());
		}
		return artists;
	}

	synchronized public ArrayList<Album> getAllAlbumsByArtist(Artist artist) {
		String[] PROJECTION = new String[] {
				"DISTINCT " + MediaStore.Audio.Media.ALBUM_ID + " AS " + MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ALBUM_ID,
				MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.YEAR,
				MediaStore.Audio.Media.ARTIST_ID,
				MediaStore.Audio.Media.ARTIST
		};
		String ORDER = MediaStore.Audio.Media.ALBUM + " COLLATE NOCASE ASC";
		String SELECTION = null;
		if (artist.getName() != getString(R.string.all_artists)) {
			SELECTION= MediaStore.Audio.Media.ARTIST_ID + " = " + artist.getId();
		}
		// else all artists was selected
		Cursor cursor = getContentResolver().query(mediaUri, PROJECTION, SELECTION, null, ORDER);

		ArrayList<Album> albums = MediaUtils.cursorToArray(Album.class, cursor);
		cursor.close();
		return albums;
	}

	synchronized public ArrayList<Song> getAllSongsInAlbum(Album album) {
		String[] PROJECTION = new String[] {
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.ALBUM_ID,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ARTIST_ID,
				MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.TRACK,
				MediaStore.Audio.Media.YEAR
		};
		// always order by artist, then album, the track
		String ORDER = MediaStore.Audio.Media.ARTIST + " COLLATE NOCASE ASC, " + MediaStore.Audio.Media.ALBUM + " COLLATE NOCASE ASC, " + MediaStore.Audio.Media.TRACK + " ASC";
		//set the selection
		String SELECTION;
		if (album.getName() == getString(R.string.all_songs)) {
			SELECTION = null;
		} else if (album.getName() == getString(R.string.all_albums)) {
			if (album.getArtistId() != -1) {
				SELECTION = MediaStore.Audio.Media.ARTIST_ID + " = " + album.getArtistId();
			} else {
				SELECTION = null;
			}
		} else {
			SELECTION = MediaStore.Audio.Media.ALBUM_ID + " = " + album.getId();
		}
		Cursor cursor = getContentResolver().query(mediaUri, PROJECTION, SELECTION, null, ORDER);

		ArrayList<Song> songs = MediaUtils.cursorToArray(Song.class, cursor);
		cursor.close();
		return songs;
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

	synchronized public State getState() {
		return mState;
	}

	synchronized public void setState(State state) {
		this.mState = state;
		sendBroadcast(new Intent(Intents.MEDIASTATE).putExtra(Intents.MEDIASTATE, state));
	}

	public void playAll() {
		// run this in a new thread as not to block
		Thread shuffle = new Thread(new Runnable() {
			@Override
			public void run() {
				stop();
				// make a new album and name "all songs"
				Album album = new Album();
				album.setName(getString(R.string.all_songs));
				// get a list of all the songs
				ArrayList<Song> allSongs = getAllSongsInAlbum(album);
				if (getShuffle()) {
					// set the start position to a random place in the list
					Random rand = new Random();
					// set the playlist
					setPlaylist(allSongs, rand.nextInt(allSongs.size()));
					// ensure shuffle is on
					setShuffle(true);
				} else {
					// start at the beginning
					setPlaylist(allSongs, 0);
					// ensure shuffle is off
					setShuffle(false);
				}
				// and play
				play();
				// as soon as we're done, stop the thread
				Thread.currentThread().interrupt();
			}
		});
		shuffle.start();
	}

	public void setRepeat(boolean repeat) {
		repeatAll = repeat;
		sharedPreferences.edit().putBoolean(Constants.REPEATALL, repeatAll).commit();
	}

	synchronized public boolean getShuffle() {
		return shuffle;
	}

	synchronized public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
		sharedPreferences.edit().putBoolean(Constants.SHUFFLE, shuffle).commit();
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
}
