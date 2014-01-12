package ca.efriesen.lydia.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.Playlists.Playlist;
import ca.efriesen.lydia.databases.Playlists.PlaylistDataSource;

/**
 * Created by eric on 1/11/2014.
 */
public class PlaylistManager extends Activity implements View.OnClickListener {

	private static final String TAG = "lydia playlist manager";
	private PlaylistDataSource dataSource;
	private Playlist playlist;
	private EditText playlistName;
	private boolean edit;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutInflater().inflate(R.layout.playlist_manager, null));

		Intent intent = getIntent();
		Button saveButton = (Button) findViewById(R.id.playlist_save);
		saveButton.setOnClickListener(this);
		playlistName = (EditText) findViewById(R.id.playlist_name);

		if (intent.hasExtra(Playlist.PLAYLIST)) {
			edit = true;
			playlist = (Playlist) intent.getSerializableExtra(Playlist.PLAYLIST);
			playlistName.setText(playlist.getName());
		} else {
			edit = false;
			playlist = new Playlist();
		}
	}

	@Override
	public void onClick(View view) {
		// hide the keyboard
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

		// save the playlist
		String name = playlistName.getText().toString();
		dataSource = new PlaylistDataSource(getApplicationContext());
		dataSource.open();
		if (edit) {
			playlist.setName(name);
			dataSource.editPlaylist(playlist);
		} else {
			// new playlist
			playlist = dataSource.createPlaylist(name);
		}
		dataSource.close();
		if (playlist.getId() != -1) {
			if (edit) {
				Toast.makeText(getApplicationContext(), "Playlist updated", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "Playlist added", Toast.LENGTH_SHORT).show();
			}
			Intent returnIntent = new Intent();
			setResult(RESULT_OK, returnIntent);
			finish();
		} else {
			Toast.makeText(getApplicationContext(), "A playlist with that name already exists", Toast.LENGTH_SHORT).show();
		}
	}
}
