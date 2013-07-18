package ca.efriesen.lydia.fragments;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia_common.media.*;
import ca.efriesen.lydia.services.MediaService;

import java.util.ArrayList;

/**
 * User: eric
 * Date: 2013-03-26
 * Time: 8:07 PM
 */
public class MusicFragment extends ListFragment {

	private Artist artist;
	private ArrayList<Artist> artists;
	private ArrayList<Album> albums;
	private ArrayList<Song> songs;
	private ArrayList<Media> medias;

	// the possible states of the view
	public static enum SELECTED {home, artist, album, songs, search};
	// default screen
	private SELECTED type = SELECTED.home;
	private int artistListPosition;

	// bind to the media service
	private boolean mediaBound = false;
	private MediaService mediaService;

	ArrayList<String> options = new ArrayList<String>();
	private static int PlaylistsID = 0;
	private static int ArtistsId = 1;
	private static int AlbumsId = 2;

	private static final String TAG = "media music fragment";

	@Override
	public void onActivityCreated(Bundle savedInstance) {
		super.onActivityCreated(savedInstance);

		options.add(PlaylistsID, getString(R.string.playlists));
		options.add(ArtistsId, getString(R.string.artists));
		options.add(AlbumsId, getString(R.string.albums));
		setHome();

		// the text input for the filter
		final EditText search = (EditText) getActivity().findViewById(R.id.filter);
		search.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				String text = search.getText().toString();

				// if the length of the text is greater than 0, we filter
				if (text.length() > 2) {
					if (type == SELECTED.artist) {
						Log.d(TAG, "artist");
						medias = mediaService.getAllLike(Artist.class, text);
					} else if (type == SELECTED.album) {
						Log.d(TAG, "album");
						medias = mediaService.getAllLike(Album.class, text);
					} else {
						Log.d(TAG, "song");
						medias = mediaService.getAllLike(Song.class, text);
					}
					setSearch();
				}
			}
		});
	}

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		Log.d(TAG, "trying to bind");
		// bind to the media service
		getActivity().bindService(new Intent(getActivity(), MediaService.class), mediaServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			getActivity().unbindService(mediaServiceConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		// hide ourself on create
		FragmentManager manager = getFragmentManager();
		manager.beginTransaction().hide(manager.findFragmentById(R.id.music_fragment)).commit();

		return inflater.inflate(R.layout.media_list_fragment, container, false);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		Log.d(TAG, "menu stuff");
		if (v.getId() == android.R.id.list) {
//			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
//			menu.setHeaderTitle(getString(R.string.playlist));
//			menu.add(Menu.NONE, 0, 0, getString(R.string.new_playlist));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// compare the title clicked, vs ones we're interested in
//		if (getString(R.string.new_playlist).equalsIgnoreCase((String)item.getTitle())) {
//			startActivity(new Intent(getActivity(), NewPlaylist.class));
//		}
		return true;
	}

	@Override
	public void onListItemClick(ListView list, View v, int position, long id) {
		// clear the text in the filter box
		EditText search = (EditText) getActivity().findViewById(R.id.filter);
		search.setText("");
		// hide keyboard
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(search.getWindowToken(), 0);

//		list.smoothScrollToPosition(0);
		if (type == SELECTED.home) {
			// if we pressed artist on the home screen
			if (Long.valueOf(ArtistsId) == id) {

				// show all artists
				setArtist();
			// else maybe albums?
			} else if (Long.valueOf(AlbumsId) == id) {
				// -1 for all albums
//				setAlbum(new Artist());
			// what about playlists
			}
		// we're in artist list
		} else if (type == SELECTED.artist) {
			// save the position, so when we come back, it's where we left off
			artistListPosition = position;
			// get the artist from the arraylist
			artist = artists.get(position);
			setAlbum(artist);
		} else if (type == SELECTED.album) {
			setSongs(albums.get(position));
		} else if (type == SELECTED.songs) {
			mediaService.stop();
			mediaService.setPlaylist(songs, position);
			mediaService.play();
		} else if(type == SELECTED.search) {

		}
	}

	// override the back button.  this allows us to switch what's displayed on screen by the back button press
	// return true as it's been handled, don't allow the default to do anything.
	// return false as we didn't do anything, you do it
	public boolean onBackPressed() {
		if (type == SELECTED.album) {
			setArtist();
			return true;
		}
		else if (type == SELECTED.songs) {
			setAlbum(artist);
			return true;
		} else if (type == SELECTED.artist) {
			setHome();
			return true;
		} else if (type == SELECTED.home) {
			FragmentManager manager = getFragmentManager();
			manager.beginTransaction()
			.hide(manager.findFragmentById(R.id.music_fragment))
			.show(manager.findFragmentById(R.id.home_screen_fragment))
			.addToBackStack(null)
			.commit();
			return true;
		} else if (type == SELECTED.search) {
			setHome();
		}
		return false;
	}

	// show the home list, has artists, playlists, albsums, whatever
	private void setHome() {
		type = SELECTED.home;

		ListView view = (ListView) getActivity().findViewById(android.R.id.list);
		// ensure we can click on things
		view.setEnabled(true);
		// set the adapter to a new array of options set above
		view.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, options));
		// get the list and register a menu listener for it
//		ListView listView = (ListView) getActivity().findViewById(android.R.id.list);
//		registerForContextMenu(listView);
	}

	private void setSearch() {
		ListView view = (ListView) getActivity().findViewById(android.R.id.list);
		// if the nothing found
		if (medias.get(0).getName() == getString(R.string.nothing_found)) {
			// make it so we can't click anything
			view.setEnabled(false);
		} else {
			view.setEnabled(true);
		}
		// set the adapter to a new array of options set above
		view.setAdapter(new ArrayAdapter<Media>(getActivity(), android.R.layout.simple_list_item_1, medias));

		type = SELECTED.search;
	}

	// shows all artists
	private void setArtist() {
//		Log.d(TAG, "set artist");
		Artist all = new Artist();
		all.setName(getString(R.string.all_artists));
		all.setId(-1);

		artists = new ArrayList<Artist>();
		artists.add(all);
		artists.addAll(mediaService.getAllArtists());

		// find the listview
		ListView view = (ListView) getActivity().findViewById(android.R.id.list);
		// set the adapter to a new array adapter of artists, and get them from the media service
		view.setAdapter(new ArrayAdapter<Artist>(getActivity(), android.R.layout.simple_list_item_1, artists));
		// set the list to the saved position
		view.setSelection(artistListPosition);

		type = SELECTED.artist;
	}

	// shows all albums by an artist
	private void setAlbum(Artist artist) {
		Log.d(TAG, "set album");
		this.artist = artist;

		Album all = new Album();
		all.setArtistId(artist.getId());
		all.setName(getString(R.string.all_albums));

		// remove all old stuff
		albums = new ArrayList<Album>();
		albums.add(all);
		albums.addAll(mediaService.getAllAlbumsByArtist(artist));

		ListView view = (ListView) getActivity().findViewById(android.R.id.list);
		view.setAdapter(new ArrayAdapter<Album>(getActivity(), android.R.layout.simple_list_item_1, albums));
		type = SELECTED.album;
	}

	// show all songs in specified album
	private void setSongs(Album album) {
		Log.d(TAG, "set songs");
		songs = mediaService.getAllSongsInAlbum(album);

		ListView view = (ListView) getActivity().findViewById(android.R.id.list);
		view.setAdapter(new ArrayAdapter<Song>(getActivity(), android.R.layout.simple_list_item_1, songs));
		type = SELECTED.songs;
	}

	private ServiceConnection mediaServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder iBinder) {
			mediaService = ((MediaService.MediaServiceBinder) iBinder).getService();
			mediaBound = true;
			Log.d(TAG, "media service bound");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mediaBound = false;
			mediaService = null;
		}
	};
/* ------------------ Begin custom classes ------------------ */

	// let's extend the simple cursor adapter so we can do our own stuff
	private class SongCursorAdapter extends SimpleCursorAdapter {
		// we want the album and artist.  we include that info in all the list items, but hide it when it's not unique
		Cursor cursor;
		Context context;
		Activity activity;
		String album;
		String prevAlbum;
		String artist;
		String prevArtist;

		public SongCursorAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to, int flags) {
			super(context, layout, cursor, from, to, flags);
			this.cursor = cursor;
			this.context = context;
			this.activity = (Activity) context;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			super.bindView(view, context, cursor);
			// get the artist and album for the current item
			album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
			artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

			// compare it with the last itme
			if (cursor.getPosition() > 0 && cursor.moveToPrevious()) {
				prevAlbum = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
				prevArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
				cursor.moveToNext();
			}

			// if the album is different, show it
			if (prevAlbum == null || !prevAlbum.equals(album) || cursor.getPosition() == 0) {
				view.findViewById(R.id.album_list).setVisibility(View.VISIBLE);
			} else {
				view.findViewById(R.id.album_list).setVisibility(View.GONE);
			}
			// if the artist is different, show it as well
			if (prevArtist == null || !prevArtist.equals(artist) || cursor.getPosition() == 0) {
				view.findViewById(R.id.artist_list).setVisibility(View.VISIBLE);
				view.findViewById(R.id.spacer).setVisibility(View.VISIBLE);
			} else {
				view.findViewById(R.id.artist_list).setVisibility(View.GONE);
				view.findViewById(R.id.spacer).setVisibility(View.GONE);
			}
		}
	}
}
