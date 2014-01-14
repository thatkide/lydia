package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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
public class AlbumState implements MusicFragmentState {

	private static final String TAG = "lydia AlbumState";
	private Activity activity;
	private MusicFragment musicFragment;
	private ArrayList albums;
	private Artist artist;
	private Album currentAlbum;
	private AlbumAdapter adapter;

	public AlbumState(MusicFragment musicFragment) {
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();
		musicFragment.localBroadcastManager.registerReceiver(mediaStateReceiver, new IntentFilter(MediaService.UPDATE_MEDIA_INFO));
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) { }

	public boolean onBackPressed() {
		musicFragment.setState(musicFragment.getArtistState());
		musicFragment.setView();
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
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
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

			TextView textView = (TextView) rowView.findViewById(android.R.id.text1);
			Album album = albums.get(position);
			textView.setText(album.getName());
			if (currentAlbum != null && album.getId() == currentAlbum.getId()) {
				textView.setTypeface(null, Typeface.BOLD_ITALIC);
			} else {
				textView.setTypeface(null, Typeface.NORMAL);
			}
			return rowView;
		}
	}
}
