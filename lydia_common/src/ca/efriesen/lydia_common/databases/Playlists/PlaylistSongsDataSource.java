package ca.efriesen.lydia_common.databases.Playlists;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import ca.efriesen.lydia_common.media.Playlist;
import ca.efriesen.lydia_common.media.Song;

import java.util.ArrayList;

/**
 * Created by eric on 1/11/2014.
 */
public class PlaylistSongsDataSource {

	private static final String TAG = "lydia playlistsongsdatasource";
	private Context context;
	private SQLiteDatabase database;
	private PlaylistSongsOpenHelper dbHelper;
	private String[] PROJECTION = {PlaylistSongsOpenHelper.COLUMN_ID, PlaylistSongsOpenHelper.PLAYLIST_ID, PlaylistSongsOpenHelper.SONG_ID, PlaylistSongsOpenHelper.ORDER};

	public PlaylistSongsDataSource(Context context) {
		this.context = context;
		dbHelper = new PlaylistSongsOpenHelper(context);
	}

	public void open() throws SQLiteException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public long getCount(Playlist playlist) {
		SQLiteStatement sqLiteStatement = database.compileStatement("SELECT COUNT(*) FROM " + PlaylistSongsOpenHelper.TABLE_NAME +  " WHERE " + PlaylistSongsOpenHelper.PLAYLIST_ID + "='" + playlist.getId() + "';");
		long count = sqLiteStatement.simpleQueryForLong();
		return count;
	}

	private int getNextOrder(Playlist playlist) {

		String SELECTION = PlaylistSongsOpenHelper.PLAYLIST_ID + " = " + playlist.getId();
		String order = PlaylistSongsOpenHelper.ORDER + " DESC";
		String limit = "1";

		Cursor cursor = database.query(
				PlaylistSongsOpenHelper.TABLE_NAME,
				null,
				SELECTION,
				null,
				null,
				null,
				order,
				limit
		);

		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			int orderNum = cursor.getInt(cursor.getColumnIndex(PlaylistSongsOpenHelper.ORDER));
			cursor.close();
			return ++orderNum;
		} else {
			cursor.close();
			return 1;
		}
	}

	public void addSongs(Playlist playlist, ArrayList<Song> songs) {
		// using the sql in this fashion vs contentValues is SIGNIFICANTLY faster.  it's not really noticeable on one insert, but when you add an entire artist to a playlist, it's very noticeable
		int startOrder = getNextOrder(playlist);
		String sql = "INSERT INTO " + PlaylistSongsOpenHelper.TABLE_NAME + " VALUES (null,?,?,?);";
		SQLiteStatement statement = database.compileStatement(sql);
		database.beginTransaction();
		for (Song song : songs) {
			statement.clearBindings();
			// song id
			statement.bindLong(1, song.getId());
			// order
			statement.bindLong(2, startOrder++);
			// playlist id
			statement.bindLong(3, playlist.getId());
			statement.execute();
		}
		database.setTransactionSuccessful();
		database.endTransaction();

	}

	public boolean addSong(Playlist playlist, Song song) {
		ContentValues values = new ContentValues();
		values.put(PlaylistSongsOpenHelper.PLAYLIST_ID, playlist.getId());
		values.put(PlaylistSongsOpenHelper.SONG_ID, song.getId());
		values.put(PlaylistSongsOpenHelper.ORDER, getNextOrder(playlist));

		long insertId = database.insert(PlaylistSongsOpenHelper.TABLE_NAME, null, values);
		// insert will return -1 on error
		if (insertId != -1) {
			// if successful, get the newly added playlist to be returned
			return true;
		} else {
			// error id
			return false;
		}
	}

	public ArrayList<Song> getSongs(Playlist playlist) {
		ArrayList<Song> songs = new ArrayList<Song>();

		String SELECTION = PlaylistSongsOpenHelper.PLAYLIST_ID + " = " + playlist.getId();
		String groupBy = null;
		String order = PlaylistSongsOpenHelper.ORDER + " ASC";

		Cursor cursor = database.query(
				PlaylistSongsOpenHelper.TABLE_NAME,
				PROJECTION,
				SELECTION,
				null,
				groupBy,
				null,
				order
		);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			int id = cursor.getInt(cursor.getColumnIndex(PlaylistSongsOpenHelper.SONG_ID));
			Song song = Song.getSong(context, id);
			songs.add(song);
			cursor.moveToNext();
		}

		cursor.close();
		return songs;
	}

	private void setOrder(Playlist playlist) {
		String SELECTION = PlaylistSongsOpenHelper.PLAYLIST_ID + " = " + playlist.getId();
		String order = PlaylistSongsOpenHelper.ORDER + " ASC";
		Cursor cursor = database.query(
				PlaylistSongsOpenHelper.TABLE_NAME,
				null,
				SELECTION,
				null,
				null,
				null,
				order
		);
		cursor.moveToFirst();

		for (int i=0; i<cursor.getCount(); i++) {
			ContentValues values = new ContentValues();
			values.put(PlaylistSongsOpenHelper.ORDER, (i+1));

			String WHERE = PlaylistSongsOpenHelper.COLUMN_ID + " = " + cursor.getInt(cursor.getColumnIndex(PlaylistSongsOpenHelper.COLUMN_ID));
			database.update(PlaylistSongsOpenHelper.TABLE_NAME, values, WHERE, null);
			cursor.moveToNext();
		}
	}

	public boolean removeAllSongs(Playlist playlist) {
		int numRows = database.delete(PlaylistSongsOpenHelper.TABLE_NAME, PlaylistSongsOpenHelper.PLAYLIST_ID + " = " + playlist.getId(), null);
		if (numRows > 0) {
			setOrder(playlist);
			return true;
		} else {
			return false;
		}
	}

	public boolean removeSong(Playlist playlist, Song song) {
		int numRows = database.delete(PlaylistSongsOpenHelper.TABLE_NAME, PlaylistSongsOpenHelper.SONG_ID + " = " + song.getId() + " AND " + PlaylistSongsOpenHelper.PLAYLIST_ID + " = " + playlist.getId(), null);
		if (numRows > 0) {
			setOrder(playlist);
			return true;
		} else {
			return false;
		}
	}

}
