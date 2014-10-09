package com.autosenseapp.fragments.MusicFragmentStates;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.autosenseapp.AutosenseApplication;
import com.autosenseapp.R;
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

	@Inject	MediaController mediaController;
	protected Activity activity;
	protected MusicFragment musicFragment;
	protected Artist artist;
	protected Album album;
	private ArrayList songs;
	private SongAdapter adapter;
	private Song currentSong;
	private ListView view;

	public SongState(MusicFragment musicFragment) {
		((AutosenseApplication)musicFragment.getActivity().getApplication().getApplicationContext()).inject(this);
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();
		musicFragment.localBroadcastManager.registerReceiver(mediaStateReceiver, new IntentFilter(MediaController.UPDATE_MEDIA_INFO));
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
			musicFragment.localBroadcastManager.unregisterReceiver(mediaStateReceiver);
		} catch (Exception e) {}
	}

	@Override
	public void onListItemClick(ListView list, View v, int position, long id) {
		mediaController.setPlaylist(songs, position);
		mediaController.play();
	}

	@Override
	public void setView(Boolean fromSearch, Media... medias) {
		// send a request to update the song info
		musicFragment.localBroadcastManager.sendBroadcast(new Intent(MediaController.GET_CURRENT_SONG));
		songs = new ArrayList<Media>(Arrays.asList(medias));
		view = (ListView) activity.findViewById(android.R.id.list);
		adapter = new SongAdapter(activity, R.layout.music_songview_row, songs);
		view.setAdapter(adapter);
		// get the list and register a menu listener for it
		musicFragment.registerForContextMenu(view);
	}

	protected void updateView(ArrayList<Song> songs) {
		this.songs.clear();
		this.songs.addAll(songs);
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
			try {
				if (musicFragment.getState() == musicFragment.getAlbumSongState()) {
					if (intent.hasExtra(MediaController.SONG)) {
						currentSong = (Song)intent.getSerializableExtra(MediaController.SONG);
						adapter.setCurrentSong(currentSong);
						int pos = songs.indexOf(currentSong);
						adapter.notifyDataSetChanged();
						view.setSelection(pos);
					}
				}
			} catch (Exception e) {
//				e.printStackTrace();
			}
		}
	};

	public class SongAdapter extends ArrayAdapter<Song> {

		private final Context context;
		private final ArrayList<Song> songs;
		private final int layoutId;
		private Song currentSong;

		public SongAdapter(Context context, int layoutId, ArrayList<Song> songs) {
			super(context, layoutId, songs);
			this.context = context;
			this.layoutId = layoutId;
			this.songs = songs;
		}

		public void setCurrentSong(Song song) {
			currentSong = song;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(layoutId, parent, false);

				viewHolder = new ViewHolder();
				viewHolder.songTrack = (TextView) convertView.findViewById(R.id.row_song_track);
				viewHolder.songTitle = (TextView) convertView.findViewById(R.id.row_song_title);
				viewHolder.songDuration = (TextView) convertView.findViewById(R.id.row_song_duration);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			Song song = songs.get(position);
			try {
				viewHolder.songTrack.setText(song.getTrack().substring(2, 4));
			} catch (StringIndexOutOfBoundsException e) {}

			viewHolder.songTitle.setText(song.getName());
			// If we populate all the songs, it's SLOW
			// If we don't we get one at a time, so it's turned off for now
//		viewHolder.songDuration.setText(song.getDurationString());

			if (currentSong != null && song.getId() == currentSong.getId()) {
				viewHolder.songTitle.setTypeface(null, Typeface.BOLD_ITALIC);
			} else {
				viewHolder.songTitle.setTypeface(null, Typeface.NORMAL);
			}
			return convertView;
		}
	}

	private static class ViewHolder {
		TextView songTrack;
		TextView songTitle;
		TextView songDuration;
	}
}