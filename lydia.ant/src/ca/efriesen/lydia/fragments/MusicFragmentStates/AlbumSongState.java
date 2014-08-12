package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.app.AlertDialog;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.alertDialogs.NewPlaylistAlert;
import ca.efriesen.lydia.fragments.MusicFragment;
import ca.efriesen.lydia_common.media.*;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by eric on 1/13/2014.
 */
public class AlbumSongState extends SongState {

	private static final String TAG = "lydia albumsongstate";

	// i keep the known menu items in the negative, because the playlist id's are all positive, but unknown
	private final int NewPlaylistId = -1;

	public AlbumSongState(MusicFragment musicFragment) {
		super(musicFragment);
	}

	@Override
	public boolean onBackPressed() {
		musicFragment.setState(musicFragment.getAlbumState());
		musicFragment.setView(artist);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		ArrayList<Playlist> playlists = Playlist.getAllPlaylists(activity);
		// it was, open the playlist context menu
		menu.setHeaderTitle(activity.getString(R.string.add_to_playlist));

		if (playlists.size() == 0) {
			menu.add(Menu.NONE, NewPlaylistId, 0, activity.getString(R.string.new_playlist));
		}
		for (Playlist playlist : playlists) {
			menu.add(Menu.NONE, playlist.getId(), 0, playlist.getName());
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// get the menu info
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		// find the listview
		ListView listView = (ListView) activity.findViewById(android.R.id.list);

		switch (item.getItemId()) {
			case NewPlaylistId: {
//				// Open the new playlist dialog
				AlertDialog.Builder builder = NewPlaylistAlert.build(activity);
				builder.show();
				break;
			}
			default: {
				// get the playlist from the listview
				Song song = (Song) listView.getItemAtPosition(info.position);
				ArrayList<Playlist> playlists = Playlist.getAllPlaylists(activity);

				for (Playlist playlist : playlists) {
					if (playlist.getId() == item.getItemId()) {
						playlist.addSong(song);
						break;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void setView(Boolean fromSearch, Media... medias) {
		ArrayList<Song> songs;
		if (!fromSearch) {
			artist = (Artist) medias[0];
			album = (Album) medias[1];
			// we need the artist for the transition back using the back button
			songs = album.getAllSongs(artist);
		} else {
			songs = new ArrayList(Arrays.asList(medias));
		}
		super.setView(fromSearch, songs.toArray(new Song[songs.size()]));
	}

}