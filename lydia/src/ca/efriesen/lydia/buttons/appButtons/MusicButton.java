package ca.efriesen.lydia.buttons.appButtons;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.fragments.MusicFragment;
import ca.efriesen.lydia.services.MediaService;
import ca.efriesen.lydia_common.media.Song;

/**
 * Created by eric on 2014-06-14.
 */
public class MusicButton extends BaseButton {

	private static final String TAG = MusicButton.class.getSimpleName();

	private LocalBroadcastManager localBroadcastManager;
	private Activity activity;

	public MusicButton(final Activity activity) {
		super(activity);
		this.activity = activity;
		localBroadcastManager = LocalBroadcastManager.getInstance(activity);
		localBroadcastManager.registerReceiver(updateMusicReceiver, new IntentFilter(MediaService.UPDATE_MEDIA_INFO));
		localBroadcastManager.sendBroadcast(new Intent(MediaService.GET_CURRENT_SONG));
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
			localBroadcastManager.unregisterReceiver(updateMusicReceiver);
		} catch (IllegalArgumentException e) { }
	}

	private BroadcastReceiver updateMusicReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				// find the music button on the home screen
				int resId = activity.getResources().getIdentifier(getResourceName(), "id", activity.getPackageName());
				android.widget.Button music = (android.widget.Button) activity.findViewById(resId);
				Button button = (Button) music.getTag();
				if (button.getButtonType() == TYPE_HOMESCREEN) {
					try {
						// set the background of the button to the album art
						Bitmap bitmap = (((Song) intent.getSerializableExtra(MediaService.SONG)).getAlbum()).getAlbumArt(activity);

						if (bitmap != null) {
							BitmapDrawable bitmapDrawable = new BitmapDrawable(activity.getResources(), bitmap);
							// create a drawable from the bitmap, and set the background of the music button to the file
							music.setBackground(bitmapDrawable);
							// remove the record image
							music.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
							// remove the text on the button
							music.setText("");
						} else {
							music.setBackgroundResource(R.drawable.button_bg);
							music.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.vinyl, 0, 0);
							music.setText(R.string.music);
						}
					} catch (Exception e) {
						music.setBackgroundResource(R.drawable.button_bg);
						music.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.vinyl, 0, 0);
						music.setText(R.string.music);
					}
				}
			} catch (Exception e) {}
		}
	};

}
