package ca.efriesen.lydia_common.media;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import ca.efriesen.lydia_common.R;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * Created by eric on 2013-06-24.
 */
public class Media implements Serializable {
	private static final String TAG = Media.class.getSimpleName();
	private Context context;
	private int id;
	private String name;
	// database stuff
	private static Uri mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

	public Media(Context context) {
		this.context = context;
	}

	public void setCursorData(Cursor cursor) {
		setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
		setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)) + ": " + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}


	public static <T extends Media> ArrayList<T> getAllLike(Class<T> clazz, Context context, ArrayList<String> search) throws ClassNotFoundException{
		String[] PROJECTION = new String[] {
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ARTIST_ID,
				MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.ALBUM_ID,
				MediaStore.Audio.Media.TRACK,
				MediaStore.Audio.Media.YEAR,
				MediaStore.Audio.Media.IS_MUSIC
		};

		String ORDER = MediaStore.Audio.Media.ARTIST + ", " + MediaStore.Audio.Media.TITLE + " asc limit 100";
		String SELECTION = MediaStore.Audio.Media.IS_MUSIC + " > 0 AND ";

		String searchString = DatabaseUtils.sqlEscapeString("%" + search.get(0) + "%");

		// Ugg, this is ugly.  I don't like having to do manual sql queries.  But it's all I can do that will get the job done.  at least for now.  maybe later something else will come along.  in the meantime, fuck it!
		if (clazz == Artist.class) {
			SELECTION += MediaStore.Audio.Media.ARTIST + " LIKE " + searchString + " ) GROUP BY ( " + MediaStore.Audio.Media.ARTIST;
		} else if (clazz == Album.class) {
			SELECTION += MediaStore.Audio.Media.ALBUM + " LIKE " + searchString +
					(search.size() == 2 ? " AND " + MediaStore.Audio.Media.ARTIST_ID + " = " + DatabaseUtils.sqlEscapeString(search.get(1)) : "" ) + " ) GROUP BY ( " + MediaStore.Audio.Media.ALBUM_ID;
		} else if (clazz == Song.class) {
			SELECTION += MediaStore.Audio.Media.TITLE + " LIKE " + searchString +
					(search.size() == 3 ? " AND " + MediaStore.Audio.Media.ARTIST_ID + " = " + DatabaseUtils.sqlEscapeString(search.get(1)) +
					" AND " + MediaStore.Audio.Media.ALBUM_ID + " = " + DatabaseUtils.sqlEscapeString(search.get(2)) : "" );
			ORDER = MediaStore.Audio.Media.TRACK + " asc";
		} else {
			throw new ClassNotFoundException();
		}

		Cursor cursor = context.getContentResolver().query(mediaUri, PROJECTION, SELECTION, null, ORDER);
		DatabaseUtils.dumpCursor(cursor);
		cursor.moveToFirst();
		// if we found 1 item or more
		if (cursor.getCount() > 0) {
			// make a new array list
			ArrayList<T> medias = MediaUtils.cursorToArray(clazz, cursor, context);
			// close the db
			cursor.close();
			// and return
			return medias;
			// we didn't find anything
		} else {
			// close the db
			cursor.close();
			// make a new list
			ArrayList<T> mediaList = new ArrayList<T>();
			// make a new media object
			// get the constructor that takes a context
			try {
				Constructor constructor = clazz.getConstructor(Context.class);

				// create a new instance of the class passed
				T media = clazz.cast(constructor.newInstance(context));
				media.setId(-1);
				media.setName(context.getString(R.string.nothing_found));
				// add it to the list
				mediaList.add(media);
				// and return
				return mediaList;
			} catch (Exception e) {}
		}
		return null;
	}

}
