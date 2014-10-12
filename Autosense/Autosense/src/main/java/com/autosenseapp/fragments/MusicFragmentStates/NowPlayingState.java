package com.autosenseapp.fragments.MusicFragmentStates;
import java.util.ArrayList;
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
	public void setView(Boolean fromSearch, Media... medias) {
		try {
			playlist = mediaController.getPlaylist();
			super.setView(false, playlist.toArray(new Song[playlist.size()]));
		} catch (Exception e) {}
	}

	@Override
	public void search(String text) {
		try {
			ArrayList<String> search = new ArrayList<String>();
			search.add(text);
			ArrayList<Song> medias = Media.getAllLike(Song.class, activity, search);
			setView(true, medias.toArray(new Song[medias.size()]));
		} catch (ClassNotFoundException e) { }
	}
}
