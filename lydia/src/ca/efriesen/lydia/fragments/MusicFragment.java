package ca.efriesen.lydia.fragments;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
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

/**
 * User: eric
 * Date: 2013-03-26
 * Time: 8:07 PM
 */
public class MusicFragment extends ListFragment {

	// bind to the media service
	public MediaService mediaService;

	private static final String TAG = "lydia media music fragment";

	private MusicFragmentState homeState;
	private MusicFragmentState artistState;
	private MusicFragmentState albumState;
	private MusicFragmentState albumSongState;
	private MusicFragmentState playlistSongState;
	private MusicFragmentState musicFragmentState;
	private MusicFragmentState playlistState;

	public LocalBroadcastManager localBroadcastManager;

	public void setState(MusicFragmentState musicFragmentState) {
		setListAdapter(null);
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

	public MusicFragmentState getAlbumSongState() {
		return albumSongState;
	}

	public MusicFragmentState getPlaylistSongState() { return playlistSongState; }

	public MusicFragmentState getPlaylistState() {
		return playlistState;
	}

	public MusicFragmentState getState() {
		return musicFragmentState;
	}


	@Override
	public void onActivityCreated(Bundle savedInstance) {
		super.onActivityCreated(savedInstance);

		localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());

		homeState = new HomeState(this);
		artistState = new ArtistState(this);
		albumState = new AlbumState(this);
		albumSongState = new AlbumSongState(this);
		playlistSongState = new PlaylistSongState(this);
		playlistState = new PlaylistState(this);
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
	public void onResume() {
		super.onResume();
		// send a broadcast to the media service asking if anything is playing.  the listview will get the info and update accordingly
		localBroadcastManager.sendBroadcast(new Intent(MediaService.GET_CURRENT_SONG));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		homeState.onDestroy();
		artistState.onDestroy();
		albumState.onDestroy();
		albumSongState.onDestroy();
		playlistSongState.onDestroy();
		playlistState.onDestroy();
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
		super.onCreateContextMenu(menu, v, menuInfo);
		musicFragmentState.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		musicFragmentState.onContextItemSelected(item);
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
		return musicFragmentState.onBackPressed();
	}

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
}
