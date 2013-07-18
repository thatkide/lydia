package ca.efriesen.lydia.plugins;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.util.Log;
import ca.efriesen.lydia_common.includes.Constants;
import ca.efriesen.lydia_common.includes.Intents;
import ca.efriesen.lydia_common.media.Song;
import ca.efriesen.lydia.services.MediaService;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;

/**
 * Created by eric on 2013-07-01.
 */
public class LastFM extends Plugin {

	private static final String TAG = "LastFM media";
	private Context context;

	// Last.fm variables
	private Session session;
	private Thread scrobbleThread;
	private Thread updateTrackThread;

	// media vars
	MediaService.State state;

	public LastFM(final Context context) {
		this.context = context;
		// if we don't want to use lastfm. return
		if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("useLastFm", false)) {
			return;
		}

		Caller.getInstance().setCache(null);

		context.registerReceiver(updateMusicReceiver, new IntentFilter(Intents.UPDATEMEDIAINFO));
		context.registerReceiver(songFinishedReceiver, new IntentFilter(Intents.SONGFINISHED));
		context.registerReceiver(mediaStateReceiver, new IntentFilter(Intents.MEDIASTATE));

		Log.d(TAG, "using lastfm");
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Log.d(TAG, "logging into last fm");
					session = Authenticator.getMobileSession(
							PreferenceManager.getDefaultSharedPreferences(context).getString("lastFmUsername", null),
							PreferenceManager.getDefaultSharedPreferences(context).getString("lastFmPassword", null),
							Constants.lastFmKey,
							Constants.lastFmSecret
					);
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
			context.unregisterReceiver(updateMusicReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			context.unregisterReceiver(songFinishedReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			context.unregisterReceiver(mediaStateReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver updateMusicReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// kill old instances
			if (updateTrackThread != null) {
				updateTrackThread.interrupt();
			}
			final Song song = (Song) intent.getSerializableExtra("ca.efriesen.Song");

			// send to last.fm in a new thread
			updateTrackThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						// until told to quit
						while (true) {
							// and we're playing
							while (state == MediaService.State.Playing) {
								try {
									// update the track info
									Track.updateNowPlaying(song.getAlbum().getArtist().getName(), song.getName(), session);
								} catch (Exception e) {
									Log.e(TAG, e.toString());
								}
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
			final Song song = (Song) intent.getExtras().getSerializable("ca.efriesen.Song");
			// new handler for scrobbling
			scrobbleThread = new Thread();
			scrobbleThread = new Thread(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "scrobble");
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

	private BroadcastReceiver mediaStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			state = (MediaService.State) intent.getExtras().getSerializable(Intents.MEDIASTATE);
		}
	};
}
