package ca.efriesen.lydia_common.media;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.io.Serializable;


/**
 * Created by eric on 2013-06-25.
 */
public class Song extends Media implements Serializable {
	private Album album;
	private Artist artist;
	private int id;
	private int album_id;
	private int artist_id;
	private int duration;
	private String durationString;
	private String name;
	private String track;

	public void setCursorData(Cursor cursor) {
		setAlbumId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
		setArtistId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)));
		setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
		setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
		setTrack(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)));
		setAlbum(cursor);
	}

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Cursor cursor) {
		album = new Album();
		album.setCursorData(cursor);
	}

	public int getAlbumId() {
		return album_id;
	}

	public void setAlbumId(int album_id) {
		this.album_id = album_id;
	}

	public int getArtistId() {
		return artist_id;
	}

	public void setArtistId(int artist_id) {
		this.artist_id = artist_id;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getDurationString() {
		return durationString;
	}

	public void setDurationString(String durationString) {
		this.durationString = durationString;
	}

	@Override
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTrack() {
		return track;
	}

	public void setTrack(String track) {
		this.track = track;
	}

	@Override
	public String toString() {
		return name;
	}

}
