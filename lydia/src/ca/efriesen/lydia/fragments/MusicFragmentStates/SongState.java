package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.fragments.MusicFragment;
import ca.efriesen.lydia.services.MediaService;
import ca.efriesen.lydia_common.media.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by eric on 1/5/2014.
 */
public class SongState implements MusicFragmentState {

	private static final String TAG = "lydia songstate";

	private Activity activity;
	private MusicFragment musicFragment;
	private ArrayList songs;
	private Artist artist;
	private Playlist playlist;
	private SongAdapter adapter;
	private Song currentSong;
	private ListView view;
	// since the songstate is so similar to the previous "playlistviewstate", i combined them and use a switch for the two of them.  i could abstact them farther and use overrides, but we'll see
	private Boolean isPlaylist = false;

	// context menu ids
	private static final int RemoveId = 0;

	public SongState(MusicFragment musicFragment) {
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();
		musicFragment.localBroadcastManager.registerReceiver(mediaStateReceiver, new IntentFilter(MediaService.UPDATE_MEDIA_INFO));
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) { }

	public boolean onBackPressed() {
		// normal view
		if (!isPlaylist) {
			musicFragment.setState(musicFragment.getAlbumState());
			musicFragment.setView(artist);
		// playlist view
		} else {
			musicFragment.setState(musicFragment.getPlaylistState());
			musicFragment.setView();
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		// normal menu
		if (!isPlaylist) {
			ArrayList<Playlist> playlists = Playlist.getAllPlaylists(activity);
			// it was, open the playlist context menu
			menu.setHeaderTitle(activity.getString(R.string.add_to_playlist));

			for (Playlist playlist : playlists) {
				menu.add(Menu.NONE, playlist.getId(), 0, playlist.getName());
			}
		// playlist menu
		} else {
			// it was, open the playlist context menu
			menu.setHeaderTitle(R.string.edit);
			menu.add(Menu.NONE, RemoveId, 0, activity.getString(R.string.remove_from_playlist));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// normal menu
		if (!isPlaylist) {
			// get the menu info
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			// find the listview
			ListView listView = (ListView) activity.findViewById(android.R.id.list);

			// get the playlist from the listview
			Song song = (Song) listView.getItemAtPosition(info.position);
			ArrayList<Playlist> playlists = Playlist.getAllPlaylists(activity);

			for (Playlist playlist : playlists) {
				if (playlist.getId() == item.getItemId()) {
					playlist.addSong(song);
					break;
				}
			}
		// playlist
		} else {
			// get the menu info
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			// find the listview
			ListView listView = (ListView) activity.findViewById(android.R.id.list);

			// what did we want to do?
			Song song = (Song) listView.getItemAtPosition(info.position);
			switch (item.getItemId()) {
				case RemoveId: {
					// remove the song from the playlist
					playlist.removeSong(song);
					updateView();
				}
			}
		}

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
		musicFragment.mediaService.setPlaylist(songs, position);
		musicFragment.mediaService.play();
	}

	@Override
	public void setView(Boolean fromSearch, Media... medias) {
		if (!fromSearch) {
			if (medias[0].getClass() != Playlist.class) {
				isPlaylist = false;
				artist = (Artist) medias[0];
				Album album = (Album) medias[1];
				// we need the artist for the transition back using the back button
				songs = album.getAllSongs(artist);
			} else {
				isPlaylist = true;
				playlist = (Playlist) medias[0];
				songs = playlist.getSongs();
			}
		} else {
			songs = new ArrayList<Media>(Arrays.asList(medias));
		}
		view = (ListView) activity.findViewById(android.R.id.list);
		adapter = new SongAdapter(activity, R.layout.music_songview_row, songs);
		view.setAdapter(adapter);
		// get the list and register a menu listener for it
		musicFragment.registerForContextMenu(view);
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

	private void updateView() {
		songs.clear();
		songs.addAll(playlist.getSongs());
		adapter.notifyDataSetChanged();
	}

	private BroadcastReceiver mediaStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (musicFragment.getState() == musicFragment.getSongState()) {
					if (intent.hasExtra(MediaService.SONG)) {
						currentSong = (Song)intent.getSerializableExtra(MediaService.SONG);
						int pos = songs.indexOf(currentSong);
						adapter.notifyDataSetChanged();
						view.setSelection(pos);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	class SongAdapter extends ArrayAdapter<Song> {

		private final Context context;
		private final ArrayList<Song> songs;
		private final int layoutId;

		public SongAdapter(Context context, int layoutId, ArrayList<Song> songs) {
			super(context, layoutId, songs);
			this.context = context;
			this.layoutId = layoutId;
			this.songs = songs;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(layoutId, parent, false);

			TextView songTrack = (TextView) rowView.findViewById(R.id.row_song_track);
			TextView songTitle = (TextView) rowView.findViewById(R.id.row_song_title);
			TextView songDuration = (TextView) rowView.findViewById(R.id.row_song_duration);

			Song song = songs.get(position);
			try {
				songTrack.setText(song.getTrack().substring(2, 4));
			}catch (StringIndexOutOfBoundsException e) {}
			songTitle.setText(song.getName());
			// If we populate all the songs, it's SLOW
			// If we don't we get one at a time, so it's turned off for now
//			songDuration.setText(song.getDurationString());

			if (currentSong != null && song.getId() == currentSong.getId()) {
				songTitle.setTypeface(null, Typeface.BOLD_ITALIC);
			} else {
				songTitle.setTypeface(null, Typeface.NORMAL);
			}
			return rowView;
		}
	}
}