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
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.autosenseapp.AutosenseApplication;
import com.autosenseapp.activities.Dashboard;
import com.autosenseapp.controllers.MediaController;
import com.autosenseapp.interfaces.NotificationInterface;
import com.autosenseapp.R;
import javax.inject.Inject;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import ca.efriesen.lydia_common.includes.Constants;
import ca.efriesen.lydia_common.media.Song;

/**
 * Created by eric on 2014-07-04.
 */
public class MusicNotificationFragment extends Fragment implements NotificationInterface {

	private static final String TAG = MusicNotificationFragment.class.getSimpleName();

	@Inject MediaController mediaController;
	@Inject LocalBroadcastManager localBroadcastManager;

	private Activity activity;
	private Song song;

	@InjectView(R.id.artist) TextView artistText;
	@InjectView(R.id.song_title) TextView songTitle;
	@InjectView(R.id.song_progress_bar) SeekBar songProgressBar;
	@InjectView(R.id.song_progress_text) TextView songProgressText;
	@InjectView(R.id.song_length) TextView songDurationText;
//	@InjectView(R.id.play_pause )ImageButton playPauseButton;
	private ImageButton playPauseButton;
	@InjectView(R.id.repeat) ImageButton repeatButton;
	@InjectView(R.id.shuffle) ImageButton shuffleButton;

	// color filters for the random and repeatButton buttons
	final PorterDuffColorFilter blueFilter = new PorterDuffColorFilter(Constants.FilterColor, PorterDuff.Mode.SRC_ATOP);
	final PorterDuffColorFilter whiteFilter = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		((AutosenseApplication)getActivity().getApplicationContext()).inject(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.notification_music_bar, container, false);
		ButterKnife.inject(this, view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		activity = getActivity();
	}

	@Override
	public void onStart() {
		super.onStart();

		playPauseButton = (ImageButton) activity.findViewById(R.id.play_pause);

		songProgressBar.setEnabled(false);
		songProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					mediaController.setCurrentPosition(progress);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// disable global gestures while changing song placement via progress bar
				((Dashboard) activity).getGestureOverlayView().removeAllOnGesturePerformedListeners();
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// re-enable gesture
				((Dashboard) activity).getGestureOverlayView().addOnGesturePerformedListener((Dashboard) activity);
			}
		});

		if (mediaController.getRepeat()) {
			repeatButton.setColorFilter(blueFilter);
		}

		if (mediaController.getShuffle()) {
			shuffleButton.setColorFilter(blueFilter);
		}

		localBroadcastManager.registerReceiver(musicInfoReceiver, new IntentFilter(MediaController.MEDIA_INFO));
	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			localBroadcastManager.unregisterReceiver(musicInfoReceiver);
		} catch (Exception e) {}
	}

	@Override
	public void saveFragment(Bundle bundle) {
	}

	@Override
	public void restoreFragment(Bundle bundle) {
		song = mediaController.getCurrentSong();
		updateSongInfo(song);
	}

	@OnClick(R.id.repeat)
	public void onRepeatClick() {
		mediaController.toggleRepeat();
		if (mediaController.getRepeat()) {
			repeatButton.setColorFilter(blueFilter);
		} else {
			repeatButton.setColorFilter(whiteFilter);
		}
	}

	@OnClick(R.id.shuffle)
	public void onShuffleClick() {
		mediaController.toggleShuffle();
		if (mediaController.getShuffle()) {
			shuffleButton.setColorFilter(blueFilter);
		} else {
			shuffleButton.setColorFilter(whiteFilter);
		}
	}

	@OnLongClick(R.id.shuffle)
	public boolean onShuffleLongClick() {
		Toast.makeText(activity, getText(R.string.shuffle_all), Toast.LENGTH_SHORT).show();
		mediaController.shufflePlay();
		// we return true, saying we've handled this.. don't let anybody else do anything
		return true;
	}

	private void updateSongInfo(Song song) {
		// update the text views
		artistText.setText(song.getAlbum().getArtist().getName() + " - " + song.getAlbum().getName());
		songTitle.setText(song.getName());

		// set the progress bar to have the same steps as the song is long in milliseconds
		songProgressBar.setEnabled(true);
		songProgressBar.setMax(song.getDuration());
		songDurationText.setText(song.getDurationString());

		// these need to be selected to start the marquee
		artistText.setSelected(true);
		songTitle.setSelected(true);

		songProgressText.setText(song.getCurrentPositionString());
		songProgressBar.setProgress(song.getCurrentPosition());

		if (song.getIsPlaying()) {
			playPauseButton.setImageResource(R.drawable.av_pause);
		} else {
			playPauseButton.setImageResource(R.drawable.av_play);
		}
	}

	private BroadcastReceiver musicInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			song = (Song) intent.getSerializableExtra(MediaController.SONG);
			updateSongInfo(song);
		}
	};
}