package ca.efriesen.lydia.activities;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia_common.includes.Intents;

import java.util.ArrayList;

/**
 * User: eric
 * Date: 2013-04-10
 * Time: 5:00 PM
 */
public class MusicSearch extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter mAdapter;
	private boolean mBound = false;

	// this is what we select from the db
	private String[] PROJECTION = new String[] {
			MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ARTIST,
			MediaStore.Audio.Media.ARTIST_ID,
			MediaStore.Audio.Media.ALBUM,
			MediaStore.Audio.Media.ALBUM_ID,
			MediaStore.Audio.Media.TRACK,
	};
	private String SELECTION;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutInflater().inflate(R.layout.music_search, null));


		String[] fromColumns = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST};
		int[] toViews = {android.R.id.text1, android.R.id.text2};

		// init an empty adapter, it will be populated in the callbacks
		mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, null, fromColumns, toViews, 0);
		setListAdapter(mAdapter);
		// init loader (unique id, args for the loader constructor, callback implementation)
		getLoaderManager().initLoader(3, null, this);

		final EditText search = (EditText) findViewById(R.id.search);
		search.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String text = search.getText().toString();
				SELECTION = MediaStore.Audio.Media.TITLE + " LIKE '%" + text + "%' OR " +
							MediaStore.Audio.Media.ARTIST + " LIKE '%" + text + "%' OR " +
							MediaStore.Audio.Media.ALBUM + " LIKE '%" + text + "%'";
				restartLoader();
			}
		});
	}

	@Override
	public void onListItemClick(ListView list, View v, int position, long id) {
		// get the cursor from the adapter
		Cursor data = mAdapter.getCursor();
		ArrayList<Integer> playlist = new ArrayList<Integer>();

		data.moveToFirst();

//		Log.d(TAG, DatabaseUtils.dumpCursorToString(data));

		while (!data.isAfterLast()) {
			playlist.add(data.getInt(data.getColumnIndex(MediaStore.Audio.Media._ID)));
			// then move to the next item in the cursor
			data.moveToNext();
		}

//		data.close();
//		Intent setPlaylist = new Intent(Intents.PLAYLIST);
//		setPlaylist.putExtra("playlist", playlist);
//		setPlaylist.putExtra("position", position);
//		sendBroadcast(setPlaylist);
//		finish();
	}


	final void restartLoader() {
		getLoaderManager().restartLoader(0, null, this);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, SELECTION, null, MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC");
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
}