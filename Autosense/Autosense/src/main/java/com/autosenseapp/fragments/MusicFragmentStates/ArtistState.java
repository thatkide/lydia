package com.autosenseapp.fragments.MusicFragmentStates;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import com.autosenseapp.AutosenseApplication;
import com.autosenseapp.adapters.MediaAdapter;
import com.autosenseapp.controllers.MediaController;
import com.autosenseapp.fragments.MusicFragment;
import ca.efriesen.lydia_common.media.Artist;
import ca.efriesen.lydia_common.media.Media;
import ca.efriesen.lydia_common.media.Song;
import java.util.ArrayList;
import java.util.Arrays;
import javax.inject.Inject;

/**
 * Created by eric on 1/5/2014.
 */
public class ArtistState implements MusicFragmentState {

	private final String TAG = ArtistState.class.getSimpleName();

	@Inject LocalBroadcastManager localBroadcastManager;
	@Inject MediaController mediaController;
	private Activity activity;
	private MusicFragment musicFragment;
	private ArrayList artists;
	private int artistListPosition;
	private Artist currentArtist, previousArtist;
	private MediaAdapter artistAdapter;

	public ArtistState(MusicFragment musicFragment) {
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();
		((AutosenseApplication)activity.getApplicationContext()).inject(this);
		localBroadcastManager.registerReceiver(mediaInfoReceiver, new IntentFilter(MediaController.MEDIA_INFO));
	}

	@Override
	public boolean onBackPressed() {
		musicFragment.setState(musicFragment.getHomeState());
		musicFragment.setView();
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) { }

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return false;
	}

	public void onDestroy() {
		try {
			localBroadcastManager.unregisterReceiver(mediaInfoReceiver);
		} catch (Exception e) {	}
	}

	@Override
	public void onListItemClick(ListView list, View v, int position, long id) {
		// save the position, so when we come back, it's where we left off
		artistListPosition = position;
		// get the artist from the arraylist
		Artist artist = (Artist) artists.get(position);
		// transition states and set the view
		musicFragment.setState(musicFragment.getAlbumState());
		musicFragment.setView(artist);
	}

	@Override
	public void setView(Boolean fromSearch, Media... medias) {
		// we only use medias if we're searching
		if (!fromSearch) {
			// default is to get all artists available
			artists = Artist.getAllArtists(activity);
		} else {
			// unless we're searching
			artists = new ArrayList<Media>(Arrays.asList(medias));
		}

		ListView listView = (ListView) activity.findViewById(android.R.id.list);
		artistAdapter = new MediaAdapter(activity, android.R.layout.simple_list_item_1, artists);

		try {
			currentArtist = mediaController.getCurrentSong().getAlbum().getArtist();
			artistAdapter.setCurrentMedia(currentArtist);
		} catch (NullPointerException e) {}
		listView.setAdapter(artistAdapter);
		listView.setSelection(artistListPosition);
	}

	@Override
	public void search(String text) {
		try {
			ArrayList<String> search = new ArrayList<String>();
			search.add(text);
			ArrayList<Artist> artists = Media.getAllLike(Artist.class, activity, search);
			setView(true, artists.toArray(new Artist[artists.size()]));
		} catch (ClassNotFoundException e) { }
	}

	private BroadcastReceiver mediaInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			currentArtist = ((Song)intent.getSerializableExtra(MediaController.SONG)).getAlbum().getArtist();
			if (currentArtist != previousArtist) {
				previousArtist = currentArtist;
				try {
					artistAdapter.setCurrentMedia(currentArtist);
					artistAdapter.notifyDataSetChanged();
				} catch (NullPointerException e) { }
			}
		}
	};
}