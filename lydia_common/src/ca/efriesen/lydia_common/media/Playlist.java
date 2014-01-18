package ca.efriesen.lydia_common.media;

import android.content.Context;
import ca.efriesen.lydia_common.databases.Playlists.PlaylistDataSource;
import ca.efriesen.lydia_common.databases.Playlists.PlaylistSongsDataSource;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by eric on 1/11/2014.
 */
public class Playlist extends Media implements Serializable {

	private static final String TAG = "lydia playlist.java";
	public static final String PLAYLIST_ID = "ca.efriesen.lydia.Playlist";

	private Context context;
	private int id;
	private String name;

	public Playlist(Context context) {
		super(context);
		this.context = context;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		name = name.trim();
		this.name = name;

		PlaylistDataSource dataSource = new PlaylistDataSource(context);
		dataSource.open();
		dataSource.createOrUpdate(this);
		dataSource.close();
	}

	public long getCount() {
		PlaylistSongsDataSource songsDataSource = new PlaylistSongsDataSource(context);
		songsDataSource.open();
		long count = songsDataSource.getCount(this);
		songsDataSource.close();
		return count;
	}

	public void clear() {
		PlaylistSongsDataSource songsDataSource = new PlaylistSongsDataSource(context);
		songsDataSource.open();
		songsDataSource.removeAllSongs(this);
		songsDataSource.close();
	}

	public void delete() {
		// remove all songs from the list
		clear();
		// then actually remove the list
		PlaylistDataSource dataSource = new PlaylistDataSource(context);
		dataSource.open();
		dataSource.deletePlaylist(this);
		dataSource.close();
	}

	public void addSong(Song song) {
		PlaylistSongsDataSource dataSource = new PlaylistSongsDataSource(context);
		dataSource.open();
		dataSource.addSong(this, song);
		dataSource.close();
	}

	public void addSongs(ArrayList<Song> songs) {
		PlaylistSongsDataSource dataSource = new PlaylistSongsDataSource(context);
		dataSource.open();
		dataSource.addSongs(this, songs);
		dataSource.close();
	}

	public ArrayList<Song> getSongs() {
		PlaylistSongsDataSource dataSource = new PlaylistSongsDataSource(context);
		dataSource.open();
		ArrayList<Song> songs = dataSource.getSongs(this);
		dataSource.close();
		return songs;
	}

	public void reOrder() {
		PlaylistSongsDataSource dataSource = new PlaylistSongsDataSource(context);
		dataSource.open();
		dataSource.setOrder(this);
		dataSource.close();
	}

	public void removeSong(int id, boolean reOrder) {
		PlaylistSongsDataSource dataSource = new PlaylistSongsDataSource(context);
		dataSource.open();
		dataSource.removeSong(this, id, reOrder);
		dataSource.close();
	}

	public void removeSong(int id) {
		PlaylistSongsDataSource dataSource = new PlaylistSongsDataSource(context);
		dataSource.open();
		dataSource.removeSong(this, id);
		dataSource.close();
	}

	public void removeSong(Song song) {
		PlaylistSongsDataSource dataSource = new PlaylistSongsDataSource(context);
		dataSource.open();
		dataSource.removeSong(this, song);
		dataSource.close();
	}

	public static Playlist get(Context context, int id) {
		PlaylistDataSource dataSource = new PlaylistDataSource(context);
		dataSource.open();
		Playlist playlist = dataSource.getPlaylist(id);
		dataSource.close();
		return playlist;
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
