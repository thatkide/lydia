package ca.efriesen.lydia_common.media;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import java.io.Serializable;
import java.util.ArrayList;


/**
 * Created by eric on 2013-06-25.
 */
public class Song extends Media implements Serializable {
	private static final String TAG = "lydia Song";
	private Context context;
	private Album album;
	private Artist artist;
	private int id;
	private int album_id;
	private int artist_id;
	private int duration;
	private String durationString;
	private String name;
	private String track;
	private static Uri mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;


	public Song(Context context) {
		super(context);
		this.context = context;
	}

	public void setCursorData(Cursor cursor) {
		setAlbumId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
		setArtistId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)));
		setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
		setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
		setTrack(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)));
		setAlbum(cursor);
//		setDuration();
	}

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Cursor cursor) {
		album = new Album(context);
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

	// This is SLOW. It's turned off for now.
	public void setDuration() {
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
		try {
			retriever.setDataSource(context, uri);
			this.duration = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
			this.durationString = MediaUtils.convertMillis(this.duration);

		} catch (Exception e) {
		e.printStackTrace();}
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

	public static ArrayList<Song> getAllSongs(Context context) {
		String[] PROJECTION = new String[] {
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.ALBUM_ID,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ARTIST_ID,
				MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.TRACK,
				MediaStore.Audio.Media.YEAR,
				MediaStore.Audio.Media.IS_MUSIC
		};
		// always order by artist, then album, the track
		String ORDER = MediaStore.Audio.Media.ARTIST + ", " + MediaStore.Audio.Media.ALBUM + ", " + MediaStore.Audio.Media.TRACK;
		//set the selection
		String SELECTION = MediaStore.Audio.Media.IS_MUSIC + " > 0";

		Cursor cursor = context.getContentResolver().query(mediaUri, PROJECTION, SELECTION, null, ORDER);
		// reset the cursor the the first item
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			ArrayList<Song> songs = new ArrayList<Song>();
			while (!cursor.isAfterLast()) {
				// create a new song object
				Song song = new Song(context);
				// pass the song the cursor data
				song.setCursorData(cursor);
				songs.add(song);
				// and return the song
				cursor.moveToNext();
			}
			cursor.close();
			return songs;
		} else {
			cursor.close();
			return null;
		}
	}

	public static Song getSong(Context context, int id) {
		String[] PROJECTION = new String[] {
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.ALBUM_ID,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ARTIST_ID,
				MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.TRACK,
				MediaStore.Audio.Media.YEAR
		};
		// always order by artist, then album, the track
		String ORDER = null;
		//set the selection
		String SELECTION = MediaStore.Audio.Media._ID + " = " + id;

		Cursor cursor = context.getContentResolver().query(mediaUri, PROJECTION, SELECTION, null, ORDER);
		// reset the cursor the the first item
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			// create a new song object
			Song song = new Song(context);
			// pass the song the cursor data
			song.setCursorData(cursor);
			// close
			cursor.close();
			// and return the song
			return song;
		} else {
			cursor.close();
			return null;
		}
	}

	@Override
	public String toString() {
		return name;
	}

}
