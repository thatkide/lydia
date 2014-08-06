package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.alertDialogs.NewPlaylistAlert;
import ca.efriesen.lydia_common.media.Playlist;
import ca.efriesen.lydia.fragments.MusicFragment;
import ca.efriesen.lydia_common.media.Media;
import ca.efriesen.lydia_common.media.Song;

import java.util.ArrayList;

/**
 * Created by eric on 1/11/2014.
 */
public class PlaylistState implements MusicFragmentState, DialogInterface.OnDismissListener {

	private static final String TAG = "lydia playlist state";

	private Activity activity;
	private MusicFragment musicFragment;
	private ArrayAdapter<Playlist> adapter;
	private ArrayList playlists;

	private static final int DeleteId = 0;
	private static final int EditId = 1;
	private static final int PlayId = 2;
	private static final int ClearId = 3;

	public PlaylistState(MusicFragment musicFragment) {
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();
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
		menu.add(Menu.NONE, PlayId, 0, activity.getString(R.string.play));
		menu.add(Menu.NONE, EditId, 0, activity.getString(R.string.edit));
		menu.add(Menu.NONE, ClearId, 0, activity.getString(R.string.clear));
		menu.add(Menu.NONE, DeleteId, 0, activity.getString(R.string.delete));
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
				builder.setMessage(activity.getString(R.string.delete_playlist_confirm) + " \"" + playlist.getName() + "\"").setTitle(activity.getString(R.string.delete));
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						playlist.delete();
						updateView();
						dialogInterface.dismiss();
					}
				});
				builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
					}
				});

				AlertDialog dialog = builder.create();
				dialog.show();
				break;
			}
			case EditId: {
				// update view
				AlertDialog.Builder builder = NewPlaylistAlert.build(activity, playlist.getId());
				builder.setOnDismissListener(this);
				builder.show();

				break;
			}
			case PlayId: {
				// play the selected list
				musicFragment.mediaService.setPlaylist(playlist.getSongs(), 0);
				musicFragment.mediaService.play();
				break;
			}
			case ClearId: {
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage(activity.getString(R.string.clear_playlist_confirm) + " \"" + playlist.getName() + "\"").setTitle(activity.getString(R.string.clear));
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						playlist.clear();
						updateView();
						dialogInterface.dismiss();
					}
				});
				builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
					}
				});

				AlertDialog dialog = builder.create();
				dialog.show();
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
		// get the artist from the arraylist
		Playlist playlist = (Playlist)playlists.get(position);
		// transition states and set the view
		musicFragment.setState(musicFragment.getPlaylistSongState());
		musicFragment.setView(playlist);

	}

	@Override
	public void setView(Boolean fromSearch, Media... medias) {
		playlists = Playlist.getAllPlaylists(activity);
		ListView view = (ListView) activity.findViewById(android.R.id.list);
		// ensure we can click on things
		view.setEnabled(true);
		// set the adapter to a new array of options set above
		adapter = new PlaylistAdapter(activity, R.layout.playlist_state_row, playlists);
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

	// This is called on dialog dismissal
	@Override
	public void onDismiss(DialogInterface dialogInterface) {
		updateView();
	}

	class PlaylistAdapter extends ArrayAdapter<Playlist> {

		private final Context context;
		private final ArrayList<Playlist> playlists;
		private final int layoutId;

		public PlaylistAdapter(Context context, int layoutId, ArrayList<Playlist> playlists) {
			super(context, layoutId, playlists);
			this.context = context;
			this.layoutId = layoutId;
			this.playlists = playlists;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(layoutId, parent, false);

				viewHolder = new ViewHolder();

				viewHolder.playlistName = (TextView) convertView.findViewById(R.id.row_playlist_name);
				viewHolder.playlistCount = (TextView) convertView.findViewById(R.id.row_playlist_song_count);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			Playlist playlist = playlists.get(position);
			viewHolder.playlistName.setText(playlist.getName());
			viewHolder.playlistCount.setText(String.valueOf(playlist.getCount()));

			return convertView;
		}
	}

	private static class ViewHolder {
		TextView playlistName;
		TextView playlistCount;
	}
}
