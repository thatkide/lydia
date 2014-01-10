package ca.efriesen.lydia_common.media;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import ca.efriesen.lydia_common.R;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
* Created by eric on 2013-06-23.
*/
public class Album extends Media implements Serializable {

	private static final String TAG = "lydia album";
	private Context context;
	private Artist artist;
	private int id;
	private String name;
	private String year;
	private int artist_id;
	// database stuff
	private Uri mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

	public Album(Context context) {
		super(context);
		this.context = context;
	}

	public void setCursorData(Cursor cursor) {
		setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
		setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
		setYear(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)));
		setArtistId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)));
		setArtist(cursor);
	}

	public void setArtist(Cursor cursor) {
		artist = new Artist(context);
		artist.setCursorData(cursor);
	}

	public Artist getArtist() {
		return artist;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	synchronized public String getName() {
		return name;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getYear() {
		return year;
	}

	public void setArtistId(int artist_id) {
		this.artist_id = artist_id;
	}

	public int getArtistId() {
		return artist_id;
	}

	public Bitmap getAlbumArt(Context context) {
		// create a new bitmap
		// get the album art uri
		final Uri artwork = Uri.parse("content://media/external/audio/albumart");

		// create a new uri for the album we're looking for
		Uri uri = ContentUris.withAppendedId(artwork, getId());

		try {
			// new file descriptor for that uri
			ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
			FileDescriptor fd = pfd.getFileDescriptor();
			// decode the file into the bitmap
			return BitmapFactory.decodeFileDescriptor(fd);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ArrayList<Song> getAllSongs() {
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
		String ORDER = MediaStore.Audio.Media.ARTIST + " COLLATE NOCASE ASC, " + MediaStore.Audio.Media.ALBUM + " COLLATE NOCASE ASC, " + MediaStore.Audio.Media.TRACK + " ASC";
		//set the selection
		String SELECTION;
		if (name == context.getString(R.string.all_songs)) {
			// Select everything marked as being music
			SELECTION = MediaStore.Audio.Media.IS_MUSIC + " != 0";
		} else if (name == context.getString(R.string.all_albums)) {
			if (artist_id != -1) {
				SELECTION = MediaStore.Audio.Media.ARTIST_ID + " = " + artist_id;
			} else {
				// Select everything markes as being music
				SELECTION = MediaStore.Audio.Media.IS_MUSIC + " != 0";
			}
		} else {
			SELECTION = MediaStore.Audio.Media.ALBUM_ID + " = " + id;
		}
		Cursor cursor = context.getContentResolver().query(mediaUri, PROJECTION, SELECTION, null, ORDER);

		ArrayList<Song> songs = MediaUtils.cursorToArray(Song.class, cursor, context);
		cursor.close();
		return songs;
	}


	@Override
	public String toString() {
		return name;
	}
}
