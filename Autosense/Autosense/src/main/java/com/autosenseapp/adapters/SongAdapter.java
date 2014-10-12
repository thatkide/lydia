package com.autosenseapp.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.autosenseapp.AutosenseApplication;
import com.autosenseapp.R;
import java.util.ArrayList;
import javax.inject.Inject;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.efriesen.lydia_common.media.Song;

/**
 * Created by eric on 2014-10-08.
 */
public class SongAdapter extends ArrayAdapter<Song> {

	private final ArrayList<Song> songs;
	private final int layoutId;
	private Song currentSong;
	@Inject LayoutInflater layoutInflater;

	public SongAdapter(Context context, int layoutId, ArrayList<Song> songs) {
		super(context, layoutId, songs);
		((AutosenseApplication)context.getApplicationContext()).inject(this);
		this.layoutId = layoutId;
		this.songs = songs;
	}

	public void setCurrentSong(Song song) {
		currentSong = song;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder viewHolder;

		if (view != null) {
			viewHolder = (ViewHolder) view.getTag();
		} else {
			view = layoutInflater.inflate(layoutId, parent, false);

			viewHolder = new ViewHolder(view);
			view.setTag(viewHolder);
		}

		Song song = songs.get(position);
		try {
			viewHolder.songTrack.setText(song.getTrack().substring(2, 4));
		} catch (StringIndexOutOfBoundsException e) {
		}

		viewHolder.songTitle.setText(song.getName());
		// If we populate all the songs, it's SLOW
		// If we don't we get one at a time, so it's turned off for now
//		viewHolder.songDuration.setText(song.getDurationString());

		if (currentSong != null && song.getId() == currentSong.getId()) {
			viewHolder.songTitle.setTypeface(null, Typeface.BOLD_ITALIC);
		} else {
			viewHolder.songTitle.setTypeface(null, Typeface.NORMAL);
		}
		return view;
	}

	static class ViewHolder {
		@InjectView(R.id.row_song_track) TextView songTrack;
		@InjectView(R.id.row_song_title) TextView songTitle;
		@InjectView(R.id.row_song_duration) TextView songDuration;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}
}