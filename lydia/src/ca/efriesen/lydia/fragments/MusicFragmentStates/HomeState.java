package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.fragments.HomeScreenFragment;
import ca.efriesen.lydia.fragments.MusicFragment;
import ca.efriesen.lydia_common.media.Media;

import java.util.ArrayList;

/**
 * Created by eric on 1/5/2014.
 */
public class HomeState implements MusicFragmentState {

	ArrayList<String> options = new ArrayList<String>();
	private static int PlaylistsID = 0;
	private static int ArtistsId = 1;
	private static int AlbumsId = 2;

	private Activity activity;
	private MusicFragment musicFragment;

	public HomeState(MusicFragment musicFragment) {
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();

		// items in the home menu are added here
		options.add(PlaylistsID, activity.getString(R.string.playlists));
		options.add(ArtistsId, activity.getString(R.string.artists));
		options.add(AlbumsId, activity.getString(R.string.albums));
	}

	@Override
	public boolean onBackPressed() {
		musicFragment.getFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.homescreen_slide_in_down, R.anim.homescreen_slide_out_down)
				.replace(R.id.home_screen_fragment, new HomeScreenFragment())
				.addToBackStack(null)
				.commit();
		return true;
	}

	@Override
	public void onDestroy() {

	}

	@Override
	public void onListItemClick(ListView list, View v, int position, long id) {
		// if we pressed artist on the home screen
			if (Long.valueOf(ArtistsId) == id) {
				// transition states and set the view
				musicFragment.setState(musicFragment.getArtistState());
				musicFragment.setView();
			// else maybe albums?
			} else if (Long.valueOf(AlbumsId) == id) {
				// -1 for all albums
				// transition states and set the view
				musicFragment.setState(musicFragment.getAlbumState());
				musicFragment.setView();
			}
		// what about playlists
	}

	@Override
	public void setView(Boolean fromSearch, Media... medias) {
		ListView view = (ListView) activity.findViewById(android.R.id.list);
		// ensure we can click on things
		view.setEnabled(true);
		// set the adapter to a new array of options set above
		view.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, options));
		// get the list and register a menu listener for it
//		ListView listView = (ListView) getActivity().findViewById(android.R.id.list);
//		registerForContextMenu(listView);

	}

	@Override
	public void search(String text) {
	}
}
