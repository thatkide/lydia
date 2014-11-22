package com.autosenseapp.fragments.MusicFragmentStates;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import com.autosenseapp.AutosenseApplication;
import com.autosenseapp.R;
import com.autosenseapp.adapters.SongAdapter;
import com.autosenseapp.controllers.MediaController;
import com.autosenseapp.fragments.MusicFragment;
import ca.efriesen.lydia_common.media.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.inject.Inject;

/**
 * Created by eric on 1/5/2014.
 */
abstract public class SongState implements MusicFragmentState {

	private static final String TAG = SongState.class.getSimpleName();

	@Inject LocalBroadcastManager localBroadcastManager;
	@Inject	MediaController mediaController;

	protected Activity activity;
	protected MusicFragment musicFragment;
	protected Artist artist;
	protected Album album;
	private ArrayList songs;
	private SongAdapter adapter;
	private Song currentSong, previousSong;
	private ListView listView;

	public SongState(MusicFragment musicFragment) {
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();
		((AutosenseApplication) activity.getApplication().getApplicationContext()).inject(this);
		localBroadcastManager.registerReceiver(mediaStateReceiver, new IntentFilter(MediaController.MEDIA_INFO));
	}

	@Override
	public boolean onBackPressed() {
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) { }

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return false;
	}

	@Override
	public void onDestroy() {
		try {
			localBroadcastManager.unregisterReceiver(mediaStateReceiver);
		} catch (Exception e) {}
	}

	@Override
	public void onListItemClick(ListView list, View v, int position, long id) {
		mediaController.setPlaylist(songs, position);
		mediaController.play();
	}

	@Override
	public void setView(Boolean fromSearch, Media... medias) {
		songs = new ArrayList<Media>(Arrays.asList(medias));
		listView = (ListView) activity.findViewById(android.R.id.list);

		adapter = new SongAdapter(activity, R.layout.music_songview_row, songs);

		try {
			currentSong = mediaController.getCurrentSong();
			adapter.setCurrentSong(currentSong);
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}

		listView.setAdapter(adapter);
		musicFragment.registerForContextMenu(listView);
	}

	protected void updateView(ArrayList<Song> songs) {
		this.songs.clear();
		this.songs.addAll(songs);
		adapter.setCurrentSong(currentSong);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void search(String text) {
		try {
			ArrayList<String> search = new ArrayList<String>();
			search.add(text);
			search.add(String.valueOf(artist.getId()));
			search.add(String.valueOf(album.getId()));
			ArrayList<Song> medias = Media.getAllLike(Song.class, activity, search);
			setView(true, medias.toArray(new Song[medias.size()]));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver mediaStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			currentSong = (Song)intent.getSerializableExtra(MediaController.SONG);
			if (currentSong != previousSong) {
				previousSong = currentSong;
				try {
					if (musicFragment.stateIsSong()) {
						if (intent.hasExtra(MediaController.SONG)) {
							adapter.setCurrentSong(currentSong);
							int pos = songs.indexOf(currentSong);
							adapter.notifyDataSetChanged();
							listView.setSelection(pos);
						}
					}
				} catch (Exception e) { }
			}
		}
	};
}