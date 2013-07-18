package ca.efriesen.lydia_common.media;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import java.io.*;
import java.nio.ByteBuffer;

/**
* Created by eric on 2013-06-23.
*/
public class Album extends Media implements Serializable {

	private static final String TAG = "lydia album";
	private Artist artist;
	private int id;
	private String name;
	private String year;
	private int artist_id;

	public void setCursorData(Cursor cursor) {
		setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
		setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
		setYear(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)));
		setArtistId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)));
		setArtist(cursor);
	}

	public void setArtist(Cursor cursor) {
		artist = new Artist();
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
	public String getName() {
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

	@Override
	public String toString() {
		return name;
	}
}
