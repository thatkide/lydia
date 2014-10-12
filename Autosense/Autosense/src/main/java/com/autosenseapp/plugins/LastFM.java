package com.autosenseapp.plugins;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.autosenseapp.R;
import com.autosenseapp.controllers.MediaController;
import ca.efriesen.lydia_common.media.Song;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;

/**
 * Created by eric on 2013-07-01.
 */
public class LastFM extends Plugin {

	private static final String TAG = LastFM.class.getSimpleName();
	private Context context;

	LocalBroadcastManager localBroadcastManager;

	// Last.fm variables
	private Session session;
	private Thread scrobbleThread;
	private Thread updateTrackThread;

	private boolean isPlaying = false;
	private boolean validSession = false;

	public LastFM(final Context context) {
		this.context = context;

		Caller.getInstance().setCache(null);

		localBroadcastManager = LocalBroadcastManager.getInstance(context);

		localBroadcastManager.registerReceiver(mediaInfoReceiver, new IntentFilter(MediaController.MEDIA_INFO));
		localBroadcastManager.registerReceiver(songFinishedReceiver, new IntentFilter(MediaController.SONG_FINISHED));

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				// if we don't want to use lastfm. return
				if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("useLastFm", false)) {
					return;
				}
				try {
					Log.d(TAG, "logging into last fm");
					session = Authenticator.getMobileSession(
							PreferenceManager.getDefaultSharedPreferences(context).getString("lastFmUsername", null),
							PreferenceManager.getDefaultSharedPreferences(context).getString("lastFmPassword", null),
							context.getString(R.string.lastFmKey),
							context.getString(R.string.lastFmSecret)
					);
					validSession = true;
					// we're done, quit the thread
					Thread.currentThread().interrupt();
					return;
				} catch (Exception e) {
					Log.d(TAG, "lastfm login failed");
					Log.d(TAG, e.toString());
					Thread.currentThread().interrupt();
					return;
				}
			}
		});
		thread.start();
	}

	public void destroy() {
		try {
			context.unregisterReceiver(mediaInfoReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			context.unregisterReceiver(songFinishedReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver mediaInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!validSession) {
				return;
			}
			// kill old instances
			if (updateTrackThread != null) {
				updateTrackThread.interrupt();
			}
			final Song song = (Song) intent.getSerializableExtra(MediaController.SONG);
			isPlaying = song.getIsPlaying();

			// send to last.fm in a new thread
			updateTrackThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						// until told to quit
						while (true) {
							// and we're playing
							while (isPlaying) {
								try {
									// update the track info
									Track.updateNowPlaying(song.getAlbum().getArtist().getName(), song.getName(), session);
								} catch (Exception e) { }
								try {
									Thread.sleep(25000);
								} catch (InterruptedException e) {
									// I said DIE
									Thread.currentThread().interrupt();
									// return, this ensures we quit
									return;
								}
							}
						}
					} catch (IllegalStateException e) {
						// YOU DIE NOW
						Thread.currentThread().interrupt();
						// ensure we quit
						return;
					}
				}
			});
			updateTrackThread.start();
		}
	};

	private BroadcastReceiver songFinishedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// ensure the "now playing" thread is dead
			if (updateTrackThread != null) {
				updateTrackThread.interrupt();
			}
			// remove old crap
			if (scrobbleThread != null) {
				scrobbleThread.interrupt();
			}
			final Song song = (Song) intent.getExtras().getSerializable("com.autosenseapp.Song");
			// new handler for scrobbling
			scrobbleThread = new Thread();
			scrobbleThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						// get the time
						int now = (int) (System.currentTimeMillis() / 1000);
						// scrobble the track
						Track.scrobble(song.getAlbum().getArtist().getName(), song.getName(), now, session);
						// and interrupt
						Thread.currentThread().interrupt();
					} catch (IllegalStateException e) {
						// commit suicide
						Thread.currentThread().interrupt();
					} catch (NullPointerException e) {
						Thread.currentThread().interrupt();
					}
					Thread.currentThread().interrupt();
				}
			});
			scrobbleThread.start();
		}
	};
}
