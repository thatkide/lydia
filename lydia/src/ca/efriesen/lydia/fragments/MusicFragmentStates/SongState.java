package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.fragments.MusicFragment;
import ca.efriesen.lydia.services.MediaService;
import ca.efriesen.lydia_common.media.Album;
import ca.efriesen.lydia_common.media.Artist;
import ca.efriesen.lydia_common.media.Media;
import ca.efriesen.lydia_common.media.Song;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by eric on 1/5/2014.
 */
public class SongState implements MusicFragmentState {

	private static final String TAG = "lydia songstate";

	private Activity activity;
	private MusicFragment musicFragment;
	private ArrayList songs;
	private Artist artist;
	private LocalBroadcastManager localBroadcastManager;
	private ListView listView;

	public SongState(MusicFragment musicFragment) {
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();
		localBroadcastManager = LocalBroadcastManager.getInstance(activity);

		localBroadcastManager.registerReceiver(mediaStateReceiver, new IntentFilter(MediaService.IS_PLAYING));
	}

	@Override
	public boolean onBackPressed() {
		musicFragment.setState(musicFragment.getAlbumState());
		musicFragment.setView(artist);
		return true;
	}

	@Override
	public void onListItemClick(ListView list, View v, int position, long id) {
		this.listView = list;
		musicFragment.mediaService.setPlaylist(songs, position);
		musicFragment.mediaService.play();
	}

	@Override
	public void setView(Boolean fromSearch, Media... medias) {
		if (!fromSearch) {
			Album album = (Album) medias[0];
			// we need the artist for the transition back using the back button
			artist = album.getArtist();
			songs = album.getAllSongs();
		} else {
			songs = new ArrayList<Media>(Arrays.asList(medias));
		}
		ListView view = (ListView) activity.findViewById(android.R.id.list);
		view.setAdapter(new ArrayAdapter<Song>(activity, android.R.layout.simple_list_item_1, songs));
	}

	@Override
	public void search(String text) {
		try {
			ArrayList<Song> medias = Media.getAllLike(Song.class, activity, text);
			setView(true, medias.toArray(new Song[medias.size()]));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver mediaStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// only do this if we've actually got a position in the extras
			if (intent.hasExtra("position")) {
				int position = intent.getIntExtra("position", 0);
				// get the number of elements in the list
				int length = listView.getChildCount();
				// loop over all of them
				for (int i=0; i<length; i++) {
					// get the text
					TextView textView = (TextView) listView.getChildAt(i).findViewById(android.R.id.text1);
					// if it's not the song playing
					if (position != i) {
						// ensure the text is normal
						textView.setTypeface(null, Typeface.NORMAL);
					} else {
						// otherwise make it bold
						textView.setTypeface(null, Typeface.BOLD_ITALIC);
					}
				}
			}
		}
	};
}
