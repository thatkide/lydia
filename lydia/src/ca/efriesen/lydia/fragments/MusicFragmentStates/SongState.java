package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ca.efriesen.lydia.fragments.MusicFragment;
import ca.efriesen.lydia.services.MediaService;
import ca.efriesen.lydia_common.media.Album;
import ca.efriesen.lydia_common.media.Artist;
import ca.efriesen.lydia_common.media.Media;
import ca.efriesen.lydia_common.media.Song;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by eric on 1/5/2014.
 */
public class SongState implements MusicFragmentState {

	private static final String TAG = "lydia songstate";

	private Activity activity;
	private MusicFragment musicFragment;
	private ArrayList songs;
	private Artist artist;
	private LocalBroadcastManager localBroadcastManager;
	private SongAdapter adapter;
	private Song currentSong;

	public SongState(MusicFragment musicFragment) {
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();
		localBroadcastManager = LocalBroadcastManager.getInstance(activity);

		localBroadcastManager.registerReceiver(mediaStateReceiver, new IntentFilter(MediaService.IS_PLAYING));
	}

	public boolean onBackPressed() {
		musicFragment.setState(musicFragment.getAlbumState());
		musicFragment.setView(artist);
		return true;
	}

	public void onListItemClick(ListView list, View v, int position, long id) {
		musicFragment.mediaService.setPlaylist(songs, position);
		musicFragment.mediaService.play();
	}

	public void setView(Boolean fromSearch, Media... medias) {
		if (!fromSearch) {
			Album album = (Album) medias[0];
			// we need the artist for the transition back using the back button
			artist = album.getArtist();
			songs = album.getAllSongs();
		} else {
			songs = new ArrayList<Media>(Arrays.asList(medias));
		}
		ListView view = (ListView) activity.findViewById(android.R.id.list);
		adapter = new SongAdapter(activity, android.R.layout.simple_list_item_1, songs);
		view.setAdapter(adapter);
	}

	public void search(String text) {
		try {
			ArrayList<Song> medias = Media.getAllLike(Song.class, activity, text);
			setView(true, medias.toArray(new Song[medias.size()]));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver mediaStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			adapter.notifyDataSetChanged();
		}
	};

	class SongAdapter extends ArrayAdapter<Song> {

		private final Context context;
		private final ArrayList<Song> songs;

		public SongAdapter(Context context, int textViewResourceId, ArrayList<Song> songs) {
			super(context, textViewResourceId, songs);
			this.context = context;
			this.songs = songs;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

			TextView textView = (TextView) rowView.findViewById(android.R.id.text1);
			Song song = songs.get(position);
			textView.setText(song.getName());
			if (currentSong != null && song.getId() == currentSong.getId()) {
				textView.setTypeface(null, Typeface.BOLD_ITALIC);
			} else {
				textView.setTypeface(null, Typeface.NORMAL);
			}
			return rowView;
		}
	}
}