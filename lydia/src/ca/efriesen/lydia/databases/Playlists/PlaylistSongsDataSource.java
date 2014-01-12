package ca.efriesen.lydia.databases.Playlists;

import android.database.sqlite.SQLiteDatabase;
import ca.efriesen.lydia.databases.MessageOpenHelper;

/**
 * Created by eric on 1/11/2014.
 */
public class PlaylistSongsDataSource {
	private SQLiteDatabase database;
	private MessageOpenHelper dbHelper;
	private String[] PROJECTION = {PlaylistSongsOpenHelper.COLUMN_ID, PlaylistSongsOpenHelper.PLAYLIST_ID, PlaylistSongsOpenHelper.SONG_ID, PlaylistSongsOpenHelper.ORDER};
}
