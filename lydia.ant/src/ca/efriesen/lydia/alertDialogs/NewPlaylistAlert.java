package ca.efriesen.lydia.alertDialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia_common.media.Playlist;

/**
 * Created by eric on 1/14/2014.
 */
public class NewPlaylistAlert {
	private static final String TAG = "lydia newplaylistalert";
	private static int playlistId = -1;

	public static AlertDialog.Builder build(final Activity activity, int... ids) {
		LayoutInflater inflater = LayoutInflater.from(activity);
		final View playlistView = inflater.inflate(R.layout.playlist_alert_dialog, null);
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final EditText name = (EditText) playlistView.findViewById(R.id.playlist_name);

		final Playlist playlist;

		// check if we've been passed a playlist id or not
		if (ids.length > 0) {
			playlistId = ids[0];
			playlist = Playlist.get(activity, playlistId);
			// we have an id, populate the edit text with the name
			name.setText(playlist.getName());
		} else {
			playlist = new Playlist(activity);
			playlist.setId(playlistId);
		}

		builder.setTitle(R.string.new_playlist)
				.setView(playlistView)
				.setCancelable(true)
				.setPositiveButton(R.string.save,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								playlist.setName(name.getText().toString());

								// hide the keyboard
								InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
								inputMethodManager.hideSoftInputFromWindow(name.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								// hide the keyboard
								InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
								inputMethodManager.hideSoftInputFromWindow(name.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
								// hide the keyboard
								dialogInterface.dismiss();
							}
						});
		return builder;
	}
}
