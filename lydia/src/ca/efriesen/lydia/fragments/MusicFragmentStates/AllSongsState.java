package ca.efriesen.lydia.fragments.MusicFragmentStates;

import java.util.ArrayList;
import ca.efriesen.lydia.fragments.MusicFragment;
import ca.efriesen.lydia_common.media.Media;
import ca.efriesen.lydia_common.media.Song;

/**
 * Created by eric on 2014-08-05.
 */
public class AllSongsState extends AlbumSongState {
	public AllSongsState(MusicFragment musicFragment) {
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
			ArrayList<Song> songs = Song.getAllSongs(activity);
			super.setView(true, songs.toArray(new Song[songs.size()]));
		} catch (NullPointerException e) {}
	}

}
