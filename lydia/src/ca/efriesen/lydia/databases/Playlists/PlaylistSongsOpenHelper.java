package ca.efriesen.lydia.databases.Playlists;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by eric on 2013-06-02.
 */
public class PlaylistSongsOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "playlistSongs.db";

	public static final String TABLE_NAME = "playlistSongs";

	public static final String COLUMN_ID = "_id";
	public static final String SONG_ID = "song_id";
	public static final String ORDER = "order";
	public static final String PLAYLIST_ID = "playlist_id";

	private static final String TABLE_CREATE =
			"CREATE TABLE " + TABLE_NAME +
					" (" + COLUMN_ID + " integer primary key autoincrement, " +
					SONG_ID + " integer not null, " +
					ORDER + " integer not null, " +
					PLAYLIST_ID + " integer not null);";

	PlaylistSongsOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		sqLiteDatabase.execSQL(TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldversion, int newversion) {
		Log.w(PlaylistOpenHelper.class.getName(), "Upgrading Database from version " + oldversion + " to " + newversion + ".  This will destroy all data.");
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(sqLiteDatabase);
	}
}
