package ca.efriesen.lydia.databases.Playlists;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by eric on 1/11/2014.
 */
public class Playlist implements Serializable {

	public static final String PLAYLIST = "ca.efriesen.lydia.Playlist";

	private long id;
	private String name;

	public Playlist() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void delete(Context context) {
		PlaylistDataSource dataSource = new PlaylistDataSource(context);
		dataSource.open();
		dataSource.deletePlaylist(this);
		dataSource.close();
	}

	public static ArrayList<Playlist> getAllPlaylists(Context context) {
		PlaylistDataSource dataSource = new PlaylistDataSource(context);
		dataSource.open();
		ArrayList<Playlist> playlists = dataSource.getPlaylists();
		dataSource.close();
		return playlists;
	}

	@Override
	public String toString() {
		return name;
	}
}
