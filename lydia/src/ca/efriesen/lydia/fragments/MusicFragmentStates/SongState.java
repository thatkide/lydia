package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.fragments.MusicFragment;
import ca.efriesen.lydia.services.MediaService;
import ca.efriesen.lydia_common.media.Album;
import ca.efriesen.lydia_common.media.Artist;
import ca.efriesen.lydia_common.media.Media;
import ca.efriesen.lydia_common.media.Song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by eric on 1/5/2014.
 */
public class SongState implements MusicFragmentState {

	private static final String TAG = "lydia songstate";

	private Activity activity;
	private MusicFragment musicFragment;
	private ArrayList songs;
	private Artist artist;
	private SongAdapter adapter;
	private Song currentSong;
	private ListView view;

	public SongState(MusicFragment musicFragment) {
		this.musicFragment = musicFragment;
		this.activity = musicFragment.getActivity();
		musicFragment.localBroadcastManager.registerReceiver(mediaStateReceiver, new IntentFilter(MediaService.UPDATE_MEDIA_INFO));
	}

	public boolean onBackPressed() {
		musicFragment.setState(musicFragment.getAlbumState());
		musicFragment.setView(artist);
		return true;
	}

	public void onDestroy() {
		try {
			musicFragment.localBroadcastManager.unregisterReceiver(mediaStateReceiver);
		} catch (Exception e) {}
	}

	public void onListItemClick(ListView list, View v, int position, long id) {
		musicFragment.mediaService.setPlaylist(songs, position);
		musicFragment.mediaService.play();
	}

	public void setView(Boolean fromSearch, Media... medias) {
		if (!fromSearch) {
			artist = (Artist) medias[0];
			Album album = (Album) medias[1];
			// we need the artist for the transition back using the back button
			songs = album.getAllSongs(artist);
		} else {
			songs = new ArrayList<Media>(Arrays.asList(medias));
		}
		view = (ListView) activity.findViewById(android.R.id.list);
		adapter = new SongAdapter(activity, R.layout.music_songview_row, songs);
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
			try {
				if (musicFragment.getState() == musicFragment.getSongState()) {
					if (intent.hasExtra(MediaService.SONG)) {
						currentSong = (Song)intent.getSerializableExtra(MediaService.SONG);
						int pos = songs.indexOf(currentSong);
						adapter.notifyDataSetChanged();
						view.setSelection(pos);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	class SongAdapter extends ArrayAdapter<Song> {

		private final Context context;
		private final ArrayList<Song> songs;
		private final int layoutId;

		public SongAdapter(Context context, int layoutId, ArrayList<Song> songs) {
			super(context, layoutId, songs);
			this.context = context;
			this.layoutId = layoutId;
			this.songs = songs;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(layoutId, parent, false);

			TextView songTrack = (TextView) rowView.findViewById(R.id.row_song_track);
			TextView songTitle = (TextView) rowView.findViewById(R.id.row_song_title);
			TextView songDuration = (TextView) rowView.findViewById(R.id.row_song_duration);

			Song song = songs.get(position);
			try {
				songTrack.setText(song.getTrack().substring(2, 4));
			}catch (StringIndexOutOfBoundsException e) {}
			songTitle.setText(song.getName());
			// If we populate all the songs, it's SLOW
			// If we don't we get one at a time, so it's turned off for now
//			songDuration.setText(song.getDurationString());

			if (currentSong != null && song.getId() == currentSong.getId()) {
				songTitle.setTypeface(null, Typeface.BOLD_ITALIC);
			} else {
				songTitle.setTypeface(null, Typeface.NORMAL);
			}
			return rowView;
		}
	}
}