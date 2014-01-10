package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ca.efriesen.lydia.R;
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

	private Activity activity;
	private MusicFragment musicFragment;
	private ArrayList artists;
	private int artistListPosition;
	private Artist currentArtist;
	private ArtistAdapter adapter;

	public ArtistState(MusicFragment musicFragment) {
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();
		musicFragment.localBroadcastManager.registerReceiver(mediaStateReceiver, new IntentFilter(MediaService.IS_PLAYING));
	}

	@Override
	public boolean onBackPressed() {
		musicFragment.setState(musicFragment.getHomeState());
		musicFragment.setView();
		return true;
	}

	@Override
	public void onDestroy() {
		try {
			musicFragment.localBroadcastManager.unregisterReceiver(mediaStateReceiver);
		} catch (Exception e) {

		}
	}

	@Override
	public void onListItemClick(ListView list, View v, int position, long id) {
		// save the position, so when we come back, it's where we left off
		artistListPosition = position;
		// get the artist from the arraylist
		Artist artist = (Artist)artists.get(position);
		// transition states and set the view
		musicFragment.setState(musicFragment.getAlbumState());
		musicFragment.setView(artist);
	}

	@Override
	public void setView(Boolean fromSearch, Media... medias) {
		if (!fromSearch) {
			// Log.d(TAG, "set artist");
			Artist all = new Artist(activity);
			all.setName(activity.getString(R.string.all_artists));
			all.setId(-1);

			artists = new ArrayList<Media>();
			artists.add(all);
			ArrayList<Artist> allArtists = Artist.getAllArtists(activity);
			if (allArtists != null) {
				artists.addAll(allArtists);
			}
		} else {
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

	@Override
	public void search(String text) {
		try {
			ArrayList<Artist> medias = Media.getAllLike(Artist.class, activity, text);
			setView(true, medias.toArray(new Artist[medias.size()]));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver mediaStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra(MediaService.SONG)) {
				currentArtist = ((Song)intent.getSerializableExtra(MediaService.SONG)).getAlbum().getArtist();
				adapter.notifyDataSetChanged();
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
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

			TextView textView = (TextView) rowView.findViewById(android.R.id.text1);
			Artist artist = artists.get(position);
			textView.setText(artist.getName());
			if (currentArtist != null && artist.getId() == currentArtist.getId()) {
				textView.setTypeface(null, Typeface.BOLD_ITALIC);
			} else {
				textView.setTypeface(null, Typeface.NORMAL);
			}
			return rowView;
		}
	}
}
