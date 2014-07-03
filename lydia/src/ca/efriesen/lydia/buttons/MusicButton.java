package ca.efriesen.lydia.buttons;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.PopupMenu;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.fragments.MusicFragment;
import ca.efriesen.lydia.services.MediaService;

/**
 * Created by eric on 2014-06-14.
 */
public class MusicButton extends BaseButton {

	private LocalBroadcastManager localBroadcastManager;
	private Activity activity;

	private PopupMenu musicPopup;
	private final static int RANDOM = 1;
	private final static int PLAYALL = 2;
	private final static int PLAYLISTS = 3;
	private final static int SEARCH = 4;

	public MusicButton(final Activity activity) {
		super(activity);
		this.activity = activity;
		localBroadcastManager = LocalBroadcastManager.getInstance(activity);
		localBroadcastManager.registerReceiver(updateMusicReceiver, new IntentFilter(MediaService.UPDATE_MEDIA_INFO));

		// create the popup window for the music button
//		musicPopup = new PopupMenu(activity.getApplicationContext(), activity.findViewById(R.id.home_1));
//		musicPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//			@Override
//			public boolean onMenuItemClick(MenuItem item) {
//				switch (item.getItemId()) {
//					case RANDOM: {
//						Toast.makeText(activity.getApplicationContext(), activity.getText(R.string.shuffle_all), Toast.LENGTH_SHORT).show();
////						activity.sendBroadcast(new Intent(Intents.SHUFFLEALL));
//						break;
//					}
//					case SEARCH: {
//						activity.startActivity(new Intent(activity, MusicSearch.class));
//						break;
//					}
//					case PLAYLISTS: {
//					}
//				}
//				return false;
//			}
//		});
		// add(GroupID, ItemID, Order, Title
//		musicPopup.getMenu().add(Menu.NONE, PLAYLISTS, Menu.NONE, R.string.playlists);
//		musicPopup.getMenu().add(Menu.NONE, RANDOM, Menu.NONE, R.string.random);
//		musicPopup.getMenu().add(Menu.NONE, SEARCH, Menu.NONE, R.string.search);

	}

	@Override
	public void onClick(View view, Button button) {
		activity.getFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.homescreen_slide_out_up, R.anim.homescreen_slide_in_up)
				.replace(R.id.home_screen_fragment, new MusicFragment(), "musicFragment")
				.addToBackStack(null)
				.commit();
	}

	@Override
	public boolean onLongClick() {
		// show the music popup window
		musicPopup.show();
		return true;
	}

	@Override
	public void cleanUp() {
		try {
			localBroadcastManager.unregisterReceiver(updateMusicReceiver);
		} catch (IllegalArgumentException e) { }
	}

	private BroadcastReceiver updateMusicReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
//			Song song = (Song) intent.getSerializableExtra(MediaService.SONG);
//			Album album = song.getAlbum();
//			try {
//				// find the music button on the home screen
//				Button music = (Button) activity.findViewById(R.id.home1);
//				try {
//					// save the album id
//					PreferenceManager.getDefaultSharedPreferences(activity).edit().putInt("currentAlbum", album.getId()).commit();
//					// set the background of the button to the album art
//
//					Bitmap bitmap = album.getAlbumArt(activity.getApplicationContext());
//
//					if (bitmap != null) {
//						BitmapDrawable bitmapDrawable = new BitmapDrawable(activity.getResources(), bitmap);
//						// create a drawable from the bitmap, and set the background of the music button to the file
//						music.setBackground(bitmapDrawable);
//						// remove the record image
//						music.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
//						// remove the text on the button
//						music.setText("");
//					} else {
//						music.setBackgroundResource(R.drawable.button_bg);
//						music.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.vinyl, 0, 0);
//						music.setText(R.string.music);
//					}
//				} catch (Exception e) {
//					music.setBackgroundResource(R.drawable.button_bg);
//					music.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.vinyl, 0, 0);
//					music.setText(R.string.music);
//				}
//			} catch (Exception e) {}
		}
	};

}
