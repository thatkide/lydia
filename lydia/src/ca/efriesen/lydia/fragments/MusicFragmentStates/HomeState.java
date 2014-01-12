package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.PlaylistManager;
import ca.efriesen.lydia.fragments.HomeScreenFragment;
import ca.efriesen.lydia.fragments.MusicFragment;
import ca.efriesen.lydia_common.media.Artist;
import ca.efriesen.lydia_common.media.Media;

import java.util.ArrayList;

/**
 * Created by eric on 1/5/2014.
 */
public class HomeState implements MusicFragmentState {

	private static final String TAG = "lydia homeState";

	ArrayList<String> options = new ArrayList<String>();
	// main menu ids
	private static final int PlaylistsID = 0;
	private static final  int ArtistsId = 1;
	private static final int AlbumsId = 2;

	// context menu ids
	private static final int NewPlaylistId = 100;

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
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {}

	@Override
	public boolean onBackPressed() {
		musicFragment.getFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.homescreen_slide_in_down, R.anim.homescreen_slide_out_down)
				.replace(R.id.home_screen_fragment, new HomeScreenFragment(), "homeScreenFragment")
				.addToBackStack(null)
				.commit();
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		// get the listview
		ListView listView = (ListView) v;
		// get the menu info
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		// get the string object of the item clicked
		String item = (String) listView.getItemAtPosition(info.position);

		// check if the clicked was the playlist
		if (item.equalsIgnoreCase(activity.getString(R.string.playlists))) {
			// it was, open the playlist context menu
			menu.setHeaderTitle(activity.getString(R.string.playlist));
			menu.add(Menu.NONE, NewPlaylistId, 0, activity.getString(R.string.new_playlist));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case NewPlaylistId: {
				// Open the new playlist dialog
				activity.startActivity(new Intent(activity, PlaylistManager.class));
				break;
			}
		}
		return false;
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public void onListItemClick(ListView list, View v, int position, long id) {
		switch ((int)(long)id) {
			case ArtistsId: {
				// if we pressed artist on the home screen
				// transition states and set the view
				musicFragment.setState(musicFragment.getArtistState());
				musicFragment.setView();
				break;
			}
			case AlbumsId: {
				// else maybe albums?
				Artist artist = new Artist(activity);
				artist.setId(-1);
				// transition states and set the view
				musicFragment.setState(musicFragment.getAlbumState());
				musicFragment.setView(artist);
				break;
			}
			case PlaylistsID: {
				musicFragment.setState(musicFragment.getPlaylistState());
				musicFragment.setView();
				break;
			}
		}
	}

	@Override
	public void setView(Boolean fromSearch, Media... medias) {
		ListView view = (ListView) activity.findViewById(android.R.id.list);
		// ensure we can click on things
		view.setEnabled(true);
		// set the adapter to a new array of options set above
		view.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, options));
		// get the list and register a menu listener for it
		musicFragment.registerForContextMenu(view);
	}

	@Override
	public void search(String text) {
	}
}
