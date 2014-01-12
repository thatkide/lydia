package ca.efriesen.lydia.databases.Playlists;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;

/**
 * Created by eric on 1/11/2014.
 */
public class PlaylistDataSource {

	private Context context;
	private SQLiteDatabase database;
	private PlaylistOpenHelper dbHelper;
	private String[] PROJECTION = {PlaylistOpenHelper.COLUMN_ID, PlaylistOpenHelper.NAME};

	public PlaylistDataSource(Context context) {
		this.context = context;
		dbHelper = new PlaylistOpenHelper(context);
	}

	public void open() throws SQLiteException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public ArrayList<Playlist> getPlaylists() {
		ArrayList<Playlist> playlists = new ArrayList<Playlist>();

		String SELECTION = null;
		String groupBy = null;
		String order = PlaylistOpenHelper.NAME + " ASC";

		Cursor cursor = database.query(
			PlaylistOpenHelper.TABLE_NAME,
			PROJECTION,
			SELECTION,
			null,
			groupBy,
			null,
			order
		);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Playlist playlist = cursorToMessage(cursor);
			playlists.add(playlist);
			cursor.moveToNext();
		}

		cursor.close();
		return playlists;
	}

	public Playlist createPlaylist(String name) {
		Playlist playlist = new Playlist();
		ContentValues values = new ContentValues();
		values.put(PlaylistOpenHelper.NAME, name);

		long insertId = database.insert(PlaylistOpenHelper.TABLE_NAME, null, values);
		// insert will return -1 on error
		if (insertId != -1) {
			// if successful, get the newly added playlist to be returned
			Cursor cursor = database.query(PlaylistOpenHelper.TABLE_NAME, PROJECTION, PlaylistOpenHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
			cursor.moveToFirst();
			playlist = cursorToMessage(cursor);
			cursor.close();
		} else {
			// error id
			playlist.setId(-1);
		}
		return playlist;
	}

	public void deletePlaylist(Playlist playlist) {
		database.delete(PlaylistOpenHelper.TABLE_NAME, PlaylistOpenHelper.COLUMN_ID + " = " + playlist.getId(), null);
	}

	public void editPlaylist(Playlist playlist) {
		ContentValues values = new ContentValues();
		values.put(PlaylistOpenHelper.NAME, playlist.getName());
		database.update(PlaylistOpenHelper.TABLE_NAME, values, PlaylistOpenHelper.COLUMN_ID + " = " + playlist.getId(), null);
	}

	private Playlist cursorToMessage(Cursor cursor) {
		Playlist playlist = new Playlist();
		playlist.setId(cursor.getLong(cursor.getColumnIndex(PlaylistOpenHelper.COLUMN_ID)));
		playlist.setName(cursor.getString(cursor.getColumnIndex(PlaylistOpenHelper.NAME)));

		return playlist;
	}

}
