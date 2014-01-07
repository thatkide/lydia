package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.fragments.MusicFragment;
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

	private Activity activity;
	private MusicFragment musicFragment;
	private ArrayList<Media> albums;
	private Artist artist;

	public AlbumState(MusicFragment musicFragment) {
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();
	}

	@Override
	public boolean onBackPressed() {
		musicFragment.setState(musicFragment.getArtistState());
		musicFragment.setView();
		return true;
	}

	@Override
	public void onListItemClick(ListView list, View v, int position, long id) {
		Album album = (Album)albums.get(position);
		// transition states and set the view
		musicFragment.setState(musicFragment.getSongState());
		musicFragment.setView(album);
	}

	@Override
	public void setView(Boolean fromSearch, Media... medias) {
		// remove all old stuff
		albums = new ArrayList<Media>();

		try {
			if (!fromSearch) {
				artist = (Artist) medias[0];
				Album all = new Album(activity);
				all.setArtistId(artist.getId());
				all.setName(activity.getString(R.string.all_albums));

				albums.add(all);
				albums.addAll(artist.getAllAlbums());
			} else {
				albums = new ArrayList<Media>(Arrays.asList(medias));
			}
		} catch (Exception e) {
			artist = new Artist(activity);
		}


		ListView view = (ListView) activity.findViewById(android.R.id.list);
		view.setAdapter(new ArrayAdapter<Media>(activity, android.R.layout.simple_list_item_1, albums));
	}

	@Override
	public void search(String text) {
		try {
			ArrayList<Album> medias = Media.getAllLike(Album.class, activity, text);
			setView(true, medias.toArray(new Album[medias.size()]));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
