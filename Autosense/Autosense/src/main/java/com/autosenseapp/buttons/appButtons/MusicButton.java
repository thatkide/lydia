package com.autosenseapp.buttons.appButtons;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import com.autosenseapp.AutosenseApplication;
import com.autosenseapp.R;
import com.autosenseapp.activities.Dashboard;
import com.autosenseapp.buttons.BaseButton;
import com.autosenseapp.controllers.MediaController;
import com.autosenseapp.databases.Button;
import com.autosenseapp.fragments.MusicFragment;
import javax.inject.Inject;
import ca.efriesen.lydia_common.media.Song;

/**
 * Created by eric on 2014-06-14.
 */
public class MusicButton extends BaseButton {

	private static final String TAG = MusicButton.class.getSimpleName();

	private Activity activity;
	@Inject LocalBroadcastManager localBroadcastManager;
	@Inject SharedPreferences sharedPreferences;

	private Song prevSong;

	public MusicButton(Activity activity) {
		super(activity);
		((AutosenseApplication)activity.getApplicationContext()).inject(this);
		this.activity = activity;
		localBroadcastManager.registerReceiver(musicInfoReceiver, new IntentFilter(MediaController.MEDIA_INFO));
	}

	@Override
	public void onClick(View view, Button button) {
		activity.getFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up, R.anim.container_slide_in_down, R.anim.container_slide_out_down)
				.replace(R.id.home_screen_fragment, new MusicFragment())
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void onStart() {
	}

	@Override
	public boolean onLongClick() {
		return true;
	}

	@Override
	public void onStop() {
		try {
			localBroadcastManager.unregisterReceiver(musicInfoReceiver);
		} catch (IllegalArgumentException e) { }
	}

	private BroadcastReceiver musicInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Song currentSong = (Song) intent.getSerializableExtra(MediaController.SONG);
			// Only do this if the song is different.  Save resources.
			if (prevSong != currentSong) {
				prevSong = currentSong;
				// Only set the image if we have it yes in the preferences, and we're in the home dashboard activity
				if (sharedPreferences.getBoolean("useAlbumArtMusicBtn", false) && activity instanceof Dashboard) {
					try {
						// find the music button on the home screen
						int resId = activity.getResources().getIdentifier(getResourceName(), "id", activity.getPackageName());
						android.widget.Button musicButton = (android.widget.Button) activity.findViewById(resId);
						Button button = (Button) musicButton.getTag();
						// Only set the image if it's actually a music button
						if (button.getButtonType() == TYPE_HOMESCREEN && button.getAction().equalsIgnoreCase(getAction())) {
							try {
								// set the background of the button to the album art
								Bitmap bitmap = currentSong.getAlbum().getAlbumArt(activity);

								if (bitmap != null) {
									BitmapDrawable bitmapDrawable = new BitmapDrawable(activity.getResources(), bitmap);
									// create a drawable from the bitmap, and set the background of the music button to the file
									musicButton.setBackground(bitmapDrawable);
									// remove the record image
									musicButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
									// remove the text on the button
									musicButton.setText("");
								} else {
									setDefault(musicButton);
								}
							} catch (Exception e) {
								setDefault(musicButton);
							}
						}
					} catch (Exception e) { }
				}
			}
		}

		private void setDefault(android.widget.Button musicButton) {
			musicButton.setBackgroundResource(R.drawable.button_bg);
			musicButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.vinyl, 0, 0);
			musicButton.setText(R.string.music);
		}
	};
}