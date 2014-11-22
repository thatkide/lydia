package ca.efriesen.lydia_common.media;

import android.content.Context;

/**
 * Created by eric on 1/11/2014.
 */
public class PlaylistSongs extends Media {

	private Context context;
	private int id;
	private int song_id;
	private int playlist_id;
	private int order;

	public PlaylistSongs(Context context) {
		super(context);
		this.context = context;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSongId() {
		return song_id;
	}

	public void setSongId(int song_id) {
		this.song_id = song_id;
	}

	public int getPlaylistId() {
		return playlist_id;
	}

	public void setPlaylistId(int playlist_id) {
		this.playlist_id = playlist_id;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
