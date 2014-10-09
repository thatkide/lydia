package com.autosenseapp.fragments.MusicFragmentStates;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import java.util.ArrayList;
import com.autosenseapp.controllers.MediaController;
import com.autosenseapp.fragments.MusicFragment;
import ca.efriesen.lydia_common.media.Media;
import ca.efriesen.lydia_common.media.Song;

/**
 * Created by eric on 2014-08-07.
 */
public class NowPlayingState extends SongState {

	private static final String TAG = NowPlayingState.class.getSimpleName();

	private ArrayList<Song> playlist;

	public NowPlayingState(MusicFragment musicFragment) {
		super(musicFragment);
	}

	@Override
	public boolean onBackPressed() {
		musicFragment.setState(musicFragment.getHomeState());
		musicFragment.setView();
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void setView(Boolean fromSearch, Media... medias) {
		if (!fromSearch) {
//			// bind to the media service, get the playlist, update the view and unbind
//			activity.bindService(new Intent(activity, MediaController.class), serviceConnection, Context.BIND_AUTO_CREATE);
		} else {
			super.setView(true, medias);
		}
	}

	@Override
	public void search(String text) {
		try {
			ArrayList<String> search = new ArrayList<String>();
			search.add(text);
			ArrayList<Song> medias = Media.getAllLike(Song.class, activity, search);
			setView(true, medias.toArray(new Song[medias.size()]));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void updateView() {
		try {
			super.setView(false, playlist.toArray(new Song[playlist.size()]));
		} catch (NullPointerException e) { }
	}
}
