package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ca.efriesen.lydia.fragments.MusicFragment;
import ca.efriesen.lydia.services.MediaService;
import ca.efriesen.lydia_common.media.Artist;
import ca.efriesen.lydia_common.media.Media;
import ca.efriesen.lydia_common.media.Song;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by eric on 1/5/2014.
 */
public class ArtistState implements MusicFragmentState {

	private final String TAG = ArtistState.class.getSimpleName();

	private Activity activity;
	private MusicFragment musicFragment;
	private ArrayList artists;
	private int artistListPosition;
	private Artist currentArtist;
	private ArtistAdapter adapter;

	public ArtistState(MusicFragment musicFragment) {
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();
		musicFragment.localBroadcastManager.registerReceiver(mediaStateReceiver, new IntentFilter(MediaService.UPDATE_MEDIA_INFO));
	}

	public boolean onBackPressed() {
		musicFragment.setState(musicFragment.getHomeState());
		musicFragment.setView();
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) { }

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return false;
	}

	public void onDestroy() {
		try {
			musicFragment.localBroadcastManager.unregisterReceiver(mediaStateReceiver);
		} catch (Exception e) {

		}
	}

	public void onListItemClick(ListView list, View v, int position, long id) {
		// save the position, so when we come back, it's where we left off
		artistListPosition = position;
		// get the artist from the arraylist
		Artist artist = (Artist)artists.get(position);
		// transition states and set the view
		musicFragment.setState(musicFragment.getAlbumState());
		musicFragment.setView(artist);
	}

	public void setView(Boolean fromSearch, Media... medias) {

		// we only use medias if we're searching
		if (!fromSearch) {
			// default is to get all artists available
			artists = Artist.getAllArtists(activity);
		} else {
			// unless we're searching
			artists = new ArrayList<Media>(Arrays.asList(medias));
		}

		// find the listview
		ListView view = (ListView) activity.findViewById(android.R.id.list);
		// set the adapter to a new array adapter of artists, and get them from the media service
		adapter = new ArtistAdapter(activity, android.R.layout.simple_list_item_1, artists);
		view.setAdapter(adapter);
		// set the list to the saved position
		view.setSelection(artistListPosition);
	}

	public void search(String text) {
		try {
			// search for artists like our string
			ArrayList<Artist> artists = Media.getAllLike(Artist.class, activity, text);
			// set the view to the returned array
			setView(true, artists.toArray(new Artist[artists.size()]));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver mediaStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (intent.hasExtra(MediaService.SONG)) {
					currentArtist = ((Song)intent.getSerializableExtra(MediaService.SONG)).getAlbum().getArtist();
					adapter.notifyDataSetChanged();
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	};

	class ArtistAdapter extends ArrayAdapter<Artist> {

		private final Context context;
		private final ArrayList<Artist> artists;

		public ArtistAdapter(Context context, int textViewResourceId, ArrayList<Artist> artists) {
			super(context, textViewResourceId, artists);
			this.context = context;
			this.artists = artists;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

				viewHolder = new ViewHolder();
				viewHolder.artistView = (TextView) convertView.findViewById(android.R.id.text1);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			Artist artist = artists.get(position);
			viewHolder.artistView.setText(artist.getName());
			if (currentArtist != null && artist.getId() == currentArtist.getId()) {
				viewHolder.artistView.setTypeface(null, Typeface.BOLD_ITALIC);
			} else {
				viewHolder.artistView.setTypeface(null, Typeface.NORMAL);
			}
			return convertView;
		}
	}

	private static class ViewHolder {
		TextView artistView;
	}
}
