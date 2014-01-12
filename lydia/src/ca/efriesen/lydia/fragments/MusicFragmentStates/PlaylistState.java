package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import ca.efriesen.lydia.databases.Playlists.Playlist;
import ca.efriesen.lydia.fragments.MusicFragment;
import ca.efriesen.lydia_common.media.Media;

import java.util.ArrayList;

/**
 * Created by eric on 1/11/2014.
 */
public class PlaylistState implements MusicFragmentState {

	private static final String TAG = "lydia playlist state";

	private Activity activity;
	private MusicFragment musicFragment;
	private ArrayAdapter<Playlist> adapter;
	private ArrayList<Playlist> playlists;

	private static final int DeleteId = 0;
	private static final int EditId = 1;

	public PlaylistState(MusicFragment musicFragment) {
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// update the arraylist and notify the adapter.
		updateView();
	}

	@Override
	public boolean onBackPressed() {
		musicFragment.setState(musicFragment.getHomeState());
		musicFragment.setView();
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		// get the listview
		ListView listView = (ListView) v;
		// get the menu info
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		// get the string object of the item clicked
		Playlist playlist = (Playlist) listView.getItemAtPosition(info.position);

		// it was, open the playlist context menu
		menu.setHeaderTitle(playlist.getName());
		menu.add(Menu.NONE, DeleteId, 0, activity.getString(R.string.delete));
		menu.add(Menu.NONE, EditId, 0, activity.getString(R.string.edit));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// get the menu info
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		// find the listview
		ListView listView = (ListView) activity.findViewById(android.R.id.list);

		// get the playlist from the listview
		final Playlist playlist = (Playlist) listView.getItemAtPosition(info.position);

		// what did we want to do?
		switch (item.getItemId()) {
			case DeleteId: {
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage("Really delete playlist " + playlist.getName()).setTitle(activity.getString(R.string.delete));
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						playlist.delete(activity);
						updateView();
					}
				});
				builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						// do nothing but close the dialog
					}
				});

				AlertDialog dialog = builder.create();
				dialog.show();
				break;
			}
			case EditId: {
				musicFragment.startActivityForResult(new Intent(activity, PlaylistManager.class).putExtra(Playlist.PLAYLIST, playlist), 1);
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

	}

	@Override
	public void setView(Boolean fromSearch, Media... medias) {
		playlists = Playlist.getAllPlaylists(activity);
		ListView view = (ListView) activity.findViewById(android.R.id.list);
		// ensure we can click on things
		view.setEnabled(true);
		// set the adapter to a new array of options set above
		adapter = new ArrayAdapter<Playlist>(activity, android.R.layout.simple_list_item_1, playlists);
		view.setAdapter(adapter);
		// get the list and register a menu listener for it
		musicFragment.registerForContextMenu(view);
	}

	@Override
	public void search(String text) {

	}

	private void updateView() {
		playlists.clear();
		playlists.addAll(Playlist.getAllPlaylists(activity));
		adapter.notifyDataSetChanged();
	}
}
