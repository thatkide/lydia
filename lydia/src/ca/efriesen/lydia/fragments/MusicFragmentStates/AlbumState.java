package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.app.Activity;
import android.app.AlertDialog;
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
import ca.efriesen.lydia.alertDialogs.NewPlaylistAlert;
import ca.efriesen.lydia.fragments.MusicFragment;
import ca.efriesen.lydia.services.MediaService;
import ca.efriesen.lydia_common.media.*;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by eric on 1/5/2014.
 */
public class AlbumState implements MusicFragmentState {

	private static final String TAG = "lydia AlbumState";
	private Activity activity;
	private MusicFragment musicFragment;
	private ArrayList albums;
	private Artist artist;
	private Album currentAlbum;
	private AlbumAdapter adapter;
	// i keep the known menu items in the negative, because the playlist id's are all positive, but unknown
	private final int NewPlaylistId = -1;

	public AlbumState(MusicFragment musicFragment) {
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();
		musicFragment.localBroadcastManager.registerReceiver(mediaStateReceiver, new IntentFilter(MediaService.UPDATE_MEDIA_INFO));
	}

	public boolean onBackPressed() {
		musicFragment.setState(musicFragment.getArtistState());
		musicFragment.setView();
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
				Album album = (Album) listView.getItemAtPosition(info.position);
				ArrayList<Playlist> playlists = Playlist.getAllPlaylists(activity);

				for (Playlist playlist : playlists) {
					if (playlist.getId() == item.getItemId()) {
						playlist.addSongs(album.getAllSongs(artist));
						break;
					}
				}
			}
		}
		return false;
	}

	public void onDestroy() {
		try {
			musicFragment.localBroadcastManager.unregisterReceiver(mediaStateReceiver);
		} catch (Exception e) {}
	}

	public void onListItemClick(ListView list, View v, int position, long id) {
		// transition states and set the view
		musicFragment.setState(musicFragment.getAlbumSongState());
		musicFragment.setView(artist, (Album)albums.get(position));
	}

	public void setView(Boolean fromSearch, Media... medias) {
		// remove all old stuff
		albums = new ArrayList<Media>();

		try {
			if (!fromSearch) {
				// we should only have one artist passed, grab it from the array
				artist = (Artist) medias[0];
				albums = artist.getAllAlbums();
			} else {
				albums = new ArrayList<Media>(Arrays.asList(medias));
			}
		} catch (Exception e) {
			e.printStackTrace();
			artist = new Artist(activity);
		}

		ListView view = (ListView) activity.findViewById(android.R.id.list);
		adapter = new AlbumAdapter(activity, android.R.layout.simple_list_item_1, albums);
		view.setAdapter(adapter);
	}

	public void search(String text) {
		try {
			ArrayList<Album> medias = Media.getAllLike(Album.class, activity, text);
			setView(true, medias.toArray(new Album[medias.size()]));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver mediaStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (intent.hasExtra(MediaService.SONG)) {
					currentAlbum = ((Song)intent.getSerializableExtra(MediaService.SONG)).getAlbum();
					adapter.notifyDataSetChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	class AlbumAdapter extends ArrayAdapter<Album> {

		private final Context context;
		private final ArrayList<Album> albums;

		public AlbumAdapter(Context context, int textViewResourceId, ArrayList<Album> albums) {
			super(context, textViewResourceId, albums);
			this.context = context;
			this.albums = albums;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

				viewHolder = new ViewHolder();
				viewHolder.album = (TextView) convertView.findViewById(android.R.id.text1);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			Album album = albums.get(position);
			viewHolder.album.setText(album.getName());
			if (currentAlbum != null && album.getId() == currentAlbum.getId()) {
				viewHolder.album.setTypeface(null, Typeface.BOLD_ITALIC);
			} else {
				viewHolder.album.setTypeface(null, Typeface.NORMAL);
			}
			return convertView;
		}
	}

	private static class ViewHolder {
		TextView album;
	}

}
