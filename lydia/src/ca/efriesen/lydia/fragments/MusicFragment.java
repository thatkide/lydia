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
import ca.efriesen.lydia.fragments.MusicFragmentStates.*;
import ca.efriesen.lydia_common.media.*;
import ca.efriesen.lydia.services.MediaService;

import java.util.ArrayList;

/**
 * User: eric
 * Date: 2013-03-26
 * Time: 8:07 PM
 */
public class MusicFragment extends ListFragment {

//	private ArrayList<Media> medias;

	// the possible states of the view
	public static enum SELECTED {home, artist, album, songs, search};

	// bind to the media service
	public MediaService mediaService;

	private static final String TAG = "lydia media music fragment";


	private MusicFragmentState homeState;
	private MusicFragmentState artistState;
	private MusicFragmentState albumState;
	private MusicFragmentState songState;
	private MusicFragmentState musicFragmentState;


	public void setState(MusicFragmentState musicFragmentState) {
		this.musicFragmentState = musicFragmentState;
	}

	public void setView(Media... medias) {
		musicFragmentState.setView(false, medias);
	}

	public MusicFragmentState getArtistState() {
		return artistState;
	}

	public MusicFragmentState getAlbumState() {
		return albumState;
	}

	public MusicFragmentState getHomeState() {
		return homeState;
	}

	public MusicFragmentState getSongState() {
		return songState;
	}

	@Override
	public void onActivityCreated(Bundle savedInstance) {
		super.onActivityCreated(savedInstance);

		homeState = new HomeState(this);
		artistState = new ArtistState(this);
		albumState = new AlbumState(this);
		songState = new SongState(this);
		musicFragmentState = homeState;
		// set to the home view
		musicFragmentState.setView(false);

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
					musicFragmentState.search(text);
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
		musicFragmentState.onListItemClick(list, v, position, id);
		// clear the text in the filter box
		EditText search = (EditText) getActivity().findViewById(R.id.filter);
		search.setText("");
		// hide keyboard
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
	}

	// override the back button.  this allows us to switch what's displayed on screen by the back button press
	// return true as it's been handled, don't allow the default to do anything.
	// return false as we didn't do anything, you do it
	public boolean onBackPressed() {
		musicFragmentState.onBackPressed();
		return false;
	}

//	private void setSearch() {
//		ListView view = (ListView) getActivity().findViewById(android.R.id.list);
//		// if the nothing found
//		if (medias.get(0).getName() == getString(R.string.nothing_found)) {
//			// make it so we can't click anything
//			view.setEnabled(false);
//		} else {
//			view.setEnabled(true);
//		}
//		// set the adapter to a new array of options set above
//		view.setAdapter(new ArrayAdapter<Media>(getActivity(), android.R.layout.simple_list_item_1, medias));
//
//		type = SELECTED.search;
//	}


	private ServiceConnection mediaServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder iBinder) {
			mediaService = ((MediaService.MediaServiceBinder) iBinder).getService();
			Log.d(TAG, "media service bound");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mediaService = null;
		}
	};
/* ------------------ Begin custom classes ------------------ */

	// let's extend the simple cursor adapter so we can do our own stuff
//	private class SongCursorAdapter extends SimpleCursorAdapter {
//		// we want the album and artist.  we include that info in all the list items, but hide it when it's not unique
//		Cursor cursor;
//		Context context;
//		Activity activity;
//		String album;
//		String prevAlbum;
//		String artist;
//		String prevArtist;
//
//		public SongCursorAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to, int flags) {
//			super(context, layout, cursor, from, to, flags);
//			this.cursor = cursor;
//			this.context = context;
//			this.activity = (Activity) context;
//		}
//
//		@Override
//		public void bindView(View view, Context context, Cursor cursor) {
//			super.bindView(view, context, cursor);
//			// get the artist and album for the current item
//			album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
//			artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//
//			// compare it with the last item
//			if (cursor.getPosition() > 0 && cursor.moveToPrevious()) {
//				prevAlbum = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
//				prevArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//				cursor.moveToNext();
//			}
//
//			// if the album is different, show it
//			if (prevAlbum == null || !prevAlbum.equals(album) || cursor.getPosition() == 0) {
//				view.findViewById(R.id.album_list).setVisibility(View.VISIBLE);
//			} else {
//				view.findViewById(R.id.album_list).setVisibility(View.GONE);
//			}
//			// if the artist is different, show it as well
//			if (prevArtist == null || !prevArtist.equals(artist) || cursor.getPosition() == 0) {
//				view.findViewById(R.id.artist_list).setVisibility(View.VISIBLE);
//				view.findViewById(R.id.spacer).setVisibility(View.VISIBLE);
//			} else {
//				view.findViewById(R.id.artist_list).setVisibility(View.GONE);
//				view.findViewById(R.id.spacer).setVisibility(View.GONE);
//			}
//		}
//	}
}
