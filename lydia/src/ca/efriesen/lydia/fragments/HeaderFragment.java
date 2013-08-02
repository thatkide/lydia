package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.*;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.includes.Helpers;
import ca.efriesen.lydia_common.includes.Constants;
import ca.efriesen.lydia_common.includes.Intents;
import ca.efriesen.lydia_common.media.Song;
import ca.efriesen.lydia.services.MediaService;

import java.util.ArrayList;


/**
 * User: eric
 * Date: 2012-10-06
 * Time: 10:31 AM
 */
public class HeaderFragment extends Fragment implements View.OnTouchListener {

	public static final String TAG = "Header Fragment";

	private Handler mHandler = new Handler();
	private Activity activity;

	private String artistId;
	private String albumId;

	// bind to the media service
	private boolean mediaBound = false;
	private MediaService mediaService;


	ImageButton home;
	ImageButton playPause;
	ImageButton previous;
	ImageButton next;
	ImageButton shuffle;
	ImageButton repeat;

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		getActivity().bindService(new Intent(getActivity(), MediaService.class), mediaServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.header_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		activity = getActivity();
	}

	@Override
	public void onStart() {
		super.onStart();

		// get all the buttons
		home = (ImageButton) activity.findViewById(R.id.home);
		playPause = (ImageButton) activity.findViewById(R.id.play_pause);
		previous = (ImageButton) activity.findViewById(R.id.previous);
		next = (ImageButton) activity.findViewById(R.id.next);
		shuffle = (ImageButton) activity.findViewById(R.id.shuffle);
		repeat = (ImageButton) activity.findViewById(R.id.repeat);

		TextView artist = (TextView) activity.findViewById(R.id.artist);
		TextView songTitle = (TextView) activity.findViewById(R.id.song_title);
		SeekBar songProgress = (SeekBar) activity.findViewById(R.id.song_progress_bar);
		songProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					mediaService.setCurrentPosition(progress);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});

		// color filters for the random and repeat buttons
		final PorterDuffColorFilter blueFilter = new PorterDuffColorFilter(Color.rgb(51, 181, 229), PorterDuff.Mode.SRC_ATOP);
		final PorterDuffColorFilter whiteFilter = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

		artist.setOnTouchListener(this);
		songTitle.setOnTouchListener(this);

		home.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// only do this if we aren't already home
				Fragment homeScreenFragment = getFragmentManager().findFragmentByTag("homeScreenFragment");
				if (!homeScreenFragment.isVisible()) {
					getFragmentManager().beginTransaction()
							.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up)
							.replace(R.id.home_screen_container_fragment, new HomeScreenContainerFragment())
							.addToBackStack(null)
							.commit();
				}
			}
		});

		playPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageButton pp = (ImageButton) activity.findViewById(R.id.play_pause);
				if (mediaService.getState() == MediaService.State.Playing) {
					pp.setImageResource(R.drawable.av_play);
					mediaService.pause();
				} else {
					pp.setImageResource(R.drawable.av_pause);
					mediaService.play();
				}
			}
		});

		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mediaService.nextSong();
			}
		});

		previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mediaService.previousSong();
			}
		});

		if (PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getBoolean(Constants.REPEATALL, false)) {
			repeat.setColorFilter(blueFilter);
		}

		repeat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Boolean repeatState = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getBoolean(Constants.REPEATALL, false);
				if (!repeatState) {
					repeat.setColorFilter(blueFilter);
				} else {
					repeat.setColorFilter(whiteFilter);
				}
				mediaService.setRepeat(!repeatState);
			}
		});

		// set the default state for the shuffle button
		if (PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getBoolean(Constants.SHUFFLE, false)) {
			shuffle.setColorFilter(blueFilter);
		}

		// set on long click to shuffle all songs
		shuffle.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Toast.makeText(activity.getApplicationContext(), getText(R.string.shuffle_all), Toast.LENGTH_SHORT).show();
				mediaService.setShuffle(true);
				mediaService.play();
				// we return true, saying we've handled this.. don't let anybody else do anything
				return true;
			}
		});

		// register the shuffle button listener
		shuffle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Boolean shuffleState = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getBoolean(Constants.SHUFFLE, false);
				if (!shuffleState) {
					shuffle.setColorFilter(blueFilter);
				} else {
					shuffle.setColorFilter(whiteFilter);
				}
				mediaService.setShuffle(!shuffleState);
			}
		});

		// register a receiver to update the media info
		activity.registerReceiver(mMusicInfo, new IntentFilter(Intents.UPDATEMEDIAINFO));

		// register a receiver to listen for the usb stick being unmounted.  when unmounted kill the update thread
		activity.registerReceiver(cardUnmountedReceiver, new IntentFilter("android.intent.action.ACTION_MEDIA_UNMOUNTED"));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// unbind on destroy
		try {
			activity.unregisterReceiver(mMusicInfo);
		} catch (Exception e) {}
		try {
			activity.unregisterReceiver(cardUnmountedReceiver);
		} catch (Exception e) {}
		try {
			getActivity().unbindService(mediaServiceConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.equals(activity.findViewById(R.id.artist))) {
//			SongListFragment songListFragment = (SongListFragment) getFragmentManager().findFragmentById(R.id.song_fragment);
//			songListFragment.showAllBy(artistId);

//			hideAllFragments().show(getFragmentManager().findFragmentById(R.id.song_fragment)).addToBackStack(null).commit();
		} else if (v.equals(activity.findViewById(R.id.song_title))) {
//			SongListFragment songListFragment = (SongListFragment) getFragmentManager().findFragmentById(R.id.song_fragment);
//			songListFragment.setAlbumId(albumId);

//			hideAllFragments().show(getFragmentManager().findFragmentById(R.id.song_fragment)).addToBackStack(null).commit();
		}

		return true;
	}

	private BroadcastReceiver cardUnmountedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// remove the callbacks
			mHandler.removeCallbacks(mUpdateTime);
			// make sure the music is stopped
//			mService.stopMusic();
			Log.d(TAG, "Card unmounted");
		}
	};

	private BroadcastReceiver mMusicInfo = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// remove old callbacks
			mHandler.removeCallbacks(mUpdateTime);

			Song song = (Song) intent.getSerializableExtra("ca.efriesen.Song");

			// get the play/pause button
			ImageButton pp = (ImageButton) activity.findViewById(R.id.play_pause);
			pp.setImageResource(R.drawable.av_pause);

			// get the required text views
			TextView artistView = (TextView) activity.findViewById(R.id.artist);
			TextView titleView = (TextView) activity.findViewById(R.id.song_title);

			// duration info
			ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.song_progress_bar);
			TextView lengthView = (TextView) activity.findViewById(R.id.song_length);

			// update the internal vars about the artist and album. so when we click the text it takes us to the correct listing
			artistId = String.valueOf(song.getArtistId());
			albumId = String.valueOf(song.getAlbumId());

			// update the text views
			artistView.setText(song.getAlbum().getArtist().getName() + " - " + song.getAlbum().getName());
			titleView.setText(song.getName());

			// set the progress bar to have the same steps as the song is long in milliseconds
			progressBar.setMax(song.getDuration());
			lengthView.setText(song.getDurationString());

			// these need to be selected to start the marquee
			artistView.setSelected(true);
			titleView.setSelected(true);

			mHandler.postDelayed(mUpdateTime, 25);
		}
	};

	private Runnable mUpdateTime = new Runnable() {
		@Override
		public void run() {
			TextView currentPosition = (TextView) activity.findViewById(R.id.song_progress_text);
			ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.song_progress_bar);

			currentPosition.setText(mediaService.getCurrentPositionString());
			progressBar.setProgress(mediaService.getCurrentPosition());

			mHandler.postDelayed(mUpdateTime, 25);
		}
	};

	private ServiceConnection mediaServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder iBinder) {
			mediaService = ((MediaService.MediaServiceBinder) iBinder).getService();
			mediaBound = true;
			Log.d(TAG, "media service bound");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mediaBound = false;
			mediaService = null;
		}
	};

}