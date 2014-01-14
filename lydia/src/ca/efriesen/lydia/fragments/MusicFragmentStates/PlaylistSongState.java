package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.app.Activity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.fragments.MusicFragment;
import ca.efriesen.lydia_common.media.Media;
import ca.efriesen.lydia_common.media.Playlist;
import ca.efriesen.lydia_common.media.Song;

import java.util.ArrayList;

/**
 * Created by eric on 1/13/2014.
 */
public class PlaylistSongState extends SongState {

	private Activity activity;
	private MusicFragment musicFragment;
	private Playlist playlist;

	// context menu ids
	private static final int RemoveId = 0;

	public PlaylistSongState(MusicFragment musicFragment) {
		super(musicFragment);
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();
	}

	@Override
	public boolean onBackPressed() {
		musicFragment.setState(musicFragment.getPlaylistState());
		musicFragment.setView();
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		// it was, open the playlist context menu
		menu.setHeaderTitle(R.string.edit);
		menu.add(Menu.NONE, RemoveId, 0, activity.getString(R.string.remove_from_playlist));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
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
				// run the update view in the super class
				super.updateView(playlist.getSongs());
			}
		}
		return false;
	}

	@Override
	public void setView(Boolean fromSearch, Media... medias) {
		playlist = (Playlist) medias[0];
		ArrayList<Song> songs = playlist.getSongs();
		super.setView(fromSearch, songs.toArray(new Song[songs.size()]));
	}
}
