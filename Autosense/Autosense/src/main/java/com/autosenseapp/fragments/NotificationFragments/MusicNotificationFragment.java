package com.autosenseapp.fragments.NotificationFragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.autosenseapp.R;
import com.autosenseapp.activities.Dashboard;
import com.autosenseapp.interfaces.NotificationInterface;
import com.autosenseapp.services.MediaService;
import ca.efriesen.lydia_common.includes.Constants;
import ca.efriesen.lydia_common.media.Song;

/**
 * Created by eric on 2014-07-04.
 */
public class MusicNotificationFragment extends Fragment implements NotificationInterface {

	private static final String TAG = MusicNotificationFragment.class.getSimpleName();

	private Activity activity;
	private Song song;
	private TextView artist;
	private TextView songTitle;
	private SeekBar songProgress;
	private TextView currentPosition;
	private TextView lengthView;

	// color filters for the random and repeat buttons
	final PorterDuffColorFilter blueFilter = new PorterDuffColorFilter(Constants.FilterColor, PorterDuff.Mode.SRC_ATOP);
	final PorterDuffColorFilter whiteFilter = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

	private LocalBroadcastManager localBroadcastManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.notification_music_bar, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		activity = getActivity();

		localBroadcastManager = LocalBroadcastManager.getInstance(activity);
	}

	@Override
	public void onStart() {
		super.onStart();

		final ImageButton shuffle = (ImageButton) activity.findViewById(R.id.shuffle);
		final ImageButton repeat = (ImageButton) activity.findViewById(R.id.repeat);
		artist = (TextView) activity.findViewById(R.id.artist);
		songTitle = (TextView) activity.findViewById(R.id.song_title);
		songProgress = (SeekBar) activity.findViewById(R.id.song_progress_bar);
		currentPosition = (TextView) activity.findViewById(R.id.song_progress_text);
		lengthView = (TextView) activity.findViewById(R.id.song_length);

		songProgress.setEnabled(false);
		songProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					localBroadcastManager.sendBroadcast(new Intent(MediaService.MEDIA_COMMAND).putExtra(MediaService.MEDIA_COMMAND, MediaService.SET_POSITION).putExtra(MediaService.SET_POSITION, progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// disable global gestures while changing volume
				((Dashboard)activity).getGestureOverlayView().removeAllOnGesturePerformedListeners();
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// re-enable gesture
				((Dashboard)activity).getGestureOverlayView().addOnGesturePerformedListener((Dashboard)activity);
			}
		});

//		artist.setOnTouchListener(this);
//		songTitle.setOnTouchListener(this);
		// we store the repeat state in shared prefs, we can read it, just don't touch it.  that's not our job
		if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(Constants.REPEATALL, false)) {
			repeat.setColorFilter(blueFilter);
		}

		repeat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				localBroadcastManager.sendBroadcast(new Intent(MediaService.MEDIA_COMMAND).putExtra(MediaService.MEDIA_COMMAND, MediaService.REPEAT));
			}
		});

		// set the default state for the shuffle button
		if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(Constants.SHUFFLE, false)) {
			shuffle.setColorFilter(blueFilter);
		}

		// set on long click to shuffle all songs
		shuffle.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Toast.makeText(activity, getText(R.string.shuffle_all), Toast.LENGTH_SHORT).show();
				localBroadcastManager.sendBroadcast(new Intent(MediaService.MEDIA_COMMAND).putExtra(MediaService.MEDIA_COMMAND, MediaService.SHUFFLE_PLAY));
				// we return true, saying we've handled this.. don't let anybody else do anything
				return true;
			}
		});

		// register the shuffle button listener
		shuffle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				localBroadcastManager.sendBroadcast(new Intent(MediaService.MEDIA_COMMAND).putExtra(MediaService.MEDIA_COMMAND, MediaService.SHUFFLE));
			}
		});

		// register a receiver to update the media info
		localBroadcastManager.registerReceiver(mMusicInfo, new IntentFilter(MediaService.UPDATE_MEDIA_INFO));

		// register the local broadcasts from the service
		localBroadcastManager.registerReceiver(mediaProgressReceiver, new IntentFilter(MediaService.PROGRESS));
		localBroadcastManager.registerReceiver(mediaRepeatState, new IntentFilter(MediaService.REPEAT_STATE));
		localBroadcastManager.registerReceiver(mediaShuffleState, new IntentFilter(MediaService.SHUFFLE_STATE));
		localBroadcastManager.registerReceiver(mediaStateReceiver, new IntentFilter(MediaService.UPDATE_MEDIA_INFO));
	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			localBroadcastManager.unregisterReceiver(mMusicInfo);
		} catch (Exception e) {}
		try {
			localBroadcastManager.unregisterReceiver(mediaProgressReceiver);
		} catch (Exception e) {}
		try {
			localBroadcastManager.unregisterReceiver(mediaRepeatState);
		} catch (Exception e) {}
		try {
			localBroadcastManager.unregisterReceiver(mediaShuffleState);
		} catch (Exception e) {}
		try {
			localBroadcastManager.unregisterReceiver(mediaStateReceiver);
		} catch (Exception e) {}

	}

//	@Override
//	public boolean onTouch(View v, MotionEvent event) {
//		if (v.equals(activity.findViewById(R.id.artist))) {
////			SongListFragment songListFragment = (SongListFragment) getFragmentManager().findFragmentById(R.id.song_fragment);
////			songListFragment.showAllBy(artistId);
//
////			hideAllFragments().show(getFragmentManager().findFragmentById(R.id.song_fragment)).addToBackStack(null).commit();
//		} else if (v.equals(activity.findViewById(R.id.song_title))) {
////			SongListFragment songListFragment = (SongListFragment) getFragmentManager().findFragmentById(R.id.song_fragment);
////			songListFragment.setAlbumId(albumId);
//
////			hideAllFragments().show(getFragmentManager().findFragmentById(R.id.song_fragment)).addToBackStack(null).commit();
//		}
//
//		return true;
//	}

	private BroadcastReceiver mMusicInfo = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			song = (Song) intent.getSerializableExtra(MediaService.SONG);

			// update the internal vars about the artist and album. so when we click the text it takes us to the correct listing
//			artistId = String.valueOf(song.getArtistId());
//			albumId = String.valueOf(song.getAlbumId());

			// update the text views
			artist.setText(song.getAlbum().getArtist().getName() + " - " + song.getAlbum().getName());
			songTitle.setText(song.getName());

			// set the progress bar to have the same steps as the song is long in milliseconds
			songProgress.setEnabled(true);
			songProgress.setMax(song.getDuration());
			lengthView.setText(song.getDurationString());

			// these need to be selected to start the marquee
			artist.setSelected(true);
			songTitle.setSelected(true);
		}
	};

	private BroadcastReceiver mediaProgressReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				currentPosition.setText(intent.getStringExtra("currentPositionString"));
				songProgress.setProgress(intent.getIntExtra("currentPositionInt", 0));
			} catch (NullPointerException e) {}
		}
	};

	private BroadcastReceiver mediaRepeatState = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final ImageButton repeat = (ImageButton) activity.findViewById(R.id.repeat);

			if (intent.getBooleanExtra(MediaService.REPEAT_STATE, false)) {
				repeat.setColorFilter(blueFilter);
			} else {
				repeat.setColorFilter(whiteFilter);
			}
		}
	};

	private BroadcastReceiver mediaShuffleState = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final ImageButton shuffle = (ImageButton) activity.findViewById(R.id.shuffle);

			if (intent.getBooleanExtra(MediaService.SHUFFLE_STATE, false)) {
				shuffle.setColorFilter(blueFilter);
			} else {
				shuffle.setColorFilter(whiteFilter);
			}
		}
	};

	private BroadcastReceiver mediaStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			ImageButton pp = (ImageButton) activity.findViewById(R.id.play_pause);
			if (intent.getBooleanExtra(MediaService.IS_PLAYING, false)) {
				pp.setImageResource(R.drawable.av_pause);
			} else {
				pp.setImageResource(R.drawable.av_play);
			}
		}
	};

	@Override
	public void saveFragment(Bundle bundle) {
	}

	@Override
	public void restoreFragment(Bundle bundle) {
		song = (Song) bundle.getSerializable("song");
		// send a broadcast asking for the current song. we'll get it and update
		localBroadcastManager.sendBroadcast(new Intent(MediaService.GET_CURRENT_SONG));
	}
}
