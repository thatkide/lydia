package ca.efriesen.lydia.includes;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia_common.media.Song;

/**
 * Created by eric on 2014-08-04.
 */
public class SongAdapter extends ArrayAdapter<Song> {

	private final Context context;
	private final ArrayList<Song> songs;
	private final int layoutId;
	private Song currentSong;

	public SongAdapter(Context context, int layoutId, ArrayList<Song> songs) {
		super(context, layoutId, songs);
		this.context = context;
		this.layoutId = layoutId;
		this.songs = songs;
	}

	static class ViewHolder {
		TextView songTrack;
		TextView songTitle;
		TextView songDuration;
	}

	public void setCurrentSong(Song song) {
		currentSong = song;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(layoutId, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.songTrack = (TextView) convertView.findViewById(R.id.row_song_track);
			viewHolder.songTitle = (TextView) convertView.findViewById(R.id.row_song_title);
			viewHolder.songDuration = (TextView) convertView.findViewById(R.id.row_song_duration);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Song song = songs.get(position);
		try {
			viewHolder.songTrack.setText(song.getTrack().substring(2, 4));
		}catch (StringIndexOutOfBoundsException e) {}

		viewHolder.songTitle.setText(song.getName());
		// If we populate all the songs, it's SLOW
		// If we don't we get one at a time, so it's turned off for now
//		viewHolder.songDuration.setText(song.getDurationString());

		if (currentSong != null && song.getId() == currentSong.getId()) {
			viewHolder.songTitle.setTypeface(null, Typeface.BOLD_ITALIC);
		} else {
			viewHolder.songTitle.setTypeface(null, Typeface.NORMAL);
		}
		return convertView;
	}
}
