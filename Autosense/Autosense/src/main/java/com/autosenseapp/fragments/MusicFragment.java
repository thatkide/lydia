package com.autosenseapp.fragments;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import com.autosenseapp.AutosenseApplication;
import com.autosenseapp.R;
import com.autosenseapp.callbacks.FragmentOnBackPressedCallback;
import com.autosenseapp.fragments.MusicFragmentStates.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.efriesen.lydia_common.media.*;

/**
 * User: eric
 * Date: 2013-03-26
 * Time: 8:07 PM
 */
public class MusicFragment extends ListFragment implements FragmentOnBackPressedCallback{

	private static final String TAG = MusicFragment.class.getSimpleName();

	private MusicFragmentState homeState;
	private MusicFragmentState allSongState;
	private MusicFragmentState artistState;
	private MusicFragmentState albumState;
	private MusicFragmentState albumSongState;
	private MusicFragmentState playlistSongState;
	private MusicFragmentState playlistState;
	private MusicFragmentState nowPlayingState;
	private MusicFragmentState musicFragmentState;

	@InjectView(R.id.filter) EditText search;
	private TextWatcher textWatcher;

	public void setState(MusicFragmentState musicFragmentState) {
		setListAdapter(null);
		this.musicFragmentState = musicFragmentState;
	}

	public void setView(Media... medias) {
		musicFragmentState.setView(false, medias);
	}

	public MusicFragmentState getArtistState() { return artistState; }

	public MusicFragmentState getAlbumState() { return albumState; }

	public MusicFragmentState getHomeState() { return homeState; }

	public MusicFragmentState getAlbumSongState() { return albumSongState; }

	public MusicFragmentState getPlaylistSongState() { return playlistSongState; }

	public MusicFragmentState getPlaylistState() { return playlistState; }

	public MusicFragmentState getAllSongsState() { return allSongState; }

	public MusicFragmentState getNowPlayingState() { return nowPlayingState; }

	public MusicFragmentState getState() { return musicFragmentState; }

	public boolean stateIsSong() {
		if (getState() instanceof SongState) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstance) {
		super.onActivityCreated(savedInstance);

		homeState = new HomeState(this);
		artistState = new ArtistState(this);
		albumState = new AlbumState(this);
		albumSongState = new AlbumSongState(this);
		playlistSongState = new PlaylistSongState(this);
		playlistState = new PlaylistState(this);
		allSongState = new AllSongsState(this);
		nowPlayingState = new NowPlayingState(this);
		musicFragmentState = homeState;
		// set to the home view
		musicFragmentState.setView(false);

		textWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				String text = search.getText().toString();
				musicFragmentState.search(text);
			}
		};

		// the text input for the filter
		search.addTextChangedListener(textWatcher);
	}

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		((AutosenseApplication)getActivity().getApplicationContext()).inject(this);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		try {
			super.onDestroy();
		} catch (NullPointerException e) {}
		try {
			homeState.onDestroy();
		} catch (NullPointerException e) {}
		try {
			artistState.onDestroy();
		} catch (NullPointerException e) {}
		try {
			albumState.onDestroy();
		} catch (NullPointerException e) {}
		try {
			albumSongState.onDestroy();
		} catch (NullPointerException e) {}
		try {
			playlistSongState.onDestroy();
		} catch (NullPointerException e) {}
		try {
			playlistState.onDestroy();
		} catch (NullPointerException e) {}
		try {
			allSongState.onDestroy();
		} catch (NullPointerException e) {}
		try {
			nowPlayingState.onDestroy();
		} catch (NullPointerException e) {}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		View view = inflater.inflate(R.layout.media_list_fragment, container, false);
		ButterKnife.inject(this, view);
		return view;
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
		// remove the listener first, otherwise it will be fired when we set the text
		search.removeTextChangedListener(textWatcher);
		search.setText("");
		// re-enable it
		search.addTextChangedListener(textWatcher);

		// hide keyboard
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
	}

	// override the back button.  this allows us to switch what's displayed on screen by the back button press
	// return true as it's been handled, don't allow the default to do anything.
	// return false as we didn't do anything, you do it
	public void onBackPressed() {
		if (getState() == homeState) {
			getFragmentManager().beginTransaction()
					.setCustomAnimations(R.anim.container_slide_in_down, R.anim.container_slide_out_down)
					.replace(R.id.home_screen_fragment, new HomeScreenFragment())
					.addToBackStack(null)
					.commit();
		} else {
			musicFragmentState.onBackPressed();
		}
	}
}
