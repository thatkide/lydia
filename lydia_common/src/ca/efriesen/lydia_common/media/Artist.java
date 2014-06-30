package ca.efriesen.lydia_common.media;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import ca.efriesen.lydia_common.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by eric on 2013-06-23.
 */
public class Artist extends Media implements Serializable {
	private static final String TAG = "Lydia Artist";
	private int id;
	private Context context;
	private String name;
	private String sortName;
	// database stuff
	private static Uri mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

	public Artist(Context context) {
		super(context);
		this.context = context;
	}

	public void setCursorData(Cursor cursor) {
		setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)));
		setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int getId() {
		return id;
	}

	// set the name
	public void setName(String name) {
		this.name = name;
		// if the name has "the " in the beginning, move it to the end
		if (name.toLowerCase().startsWith("the ")) {
			name = name.substring(4, name.length()) + ", The";
		}
		this.sortName = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getSortName() {
		return sortName;
	}

	public ArrayList<Album> getAllAlbums() {
		String[] PROJECTION = new String[] {
				"DISTINCT " + MediaStore.Audio.Media.ALBUM_ID + " AS " + MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ALBUM_ID,
				MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.YEAR,
				MediaStore.Audio.Media.ARTIST_ID,
				MediaStore.Audio.Media.ARTIST
		};
		String ORDER = MediaStore.Audio.Media.ALBUM + " COLLATE NOCASE ASC";
		String SELECTION = MediaStore.Audio.Media.IS_MUSIC + " > 0";
		if (id != -1) {
			SELECTION = MediaStore.Audio.Media.ARTIST_ID + " = " + id;
		}
		// else all artists was selected
		Cursor cursor = context.getContentResolver().query(mediaUri, PROJECTION, SELECTION, null, ORDER);

		ArrayList<Album> albums = MediaUtils.cursorToArray(Album.class, cursor, context);
		cursor.close();

		// all album option in the top of the list
		Album all = new Album(context);
		all.setId(-1);
		all.setArtistId(-1);
		all.setName(context.getString(R.string.all_albums));

		ArrayList<Album> fullList = new ArrayList<Album>();
		fullList.add(all);
		// if we got any albums, add them to the array list
		if (albums != null) {
			fullList.addAll(albums);
		}
		return fullList;
	}

	@Override
	public String toString() {
		return name;
	}

	public static ArrayList<Artist> getAllArtists(Context context) {
		String[] PROJECTION = new String[] {
				"DISTINCT " + MediaStore.Audio.Media.ARTIST_ID + " AS " + MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ARTIST_ID,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ARTIST_KEY,
				MediaStore.Audio.Media.IS_MUSIC
		};
		String ORDER = MediaStore.Audio.Media.ARTIST + " COLLATE NOCASE ASC";
		String SELECTION = MediaStore.Audio.Media.IS_MUSIC + " > 0";
		Cursor cursor = context.getContentResolver().query(mediaUri, PROJECTION, SELECTION, null, ORDER);

		ArrayList<Artist> artists = MediaUtils.cursorToArray(Artist.class, cursor, context);
		cursor.close();

		// all artists option in the list
		Artist all = new Artist(context);
		all.setName(context.getString(R.string.all_artists));
		all.setId(-1);

		ArrayList<Artist> fullList = new ArrayList<Artist>();
		fullList.add(all);
		// check if our artist arraylist is null or not.  if it isn't, sort it and add it to the list to be returned
		if (artists != null) {
			// since the db won't ignore "the" when sorting, we set the artist name to move "the" to the end, and then resort
			Collections.sort(artists, new Comparator<Artist>() {
				@Override
				public int compare(Artist artist, Artist artist2) {
					return artist.getSortName().compareToIgnoreCase(artist2.getSortName());
				}
			});
			fullList.addAll(artists);
		}

		return fullList;
	}
}
