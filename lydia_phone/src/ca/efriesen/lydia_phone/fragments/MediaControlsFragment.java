package ca.efriesen.lydia_phone.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import ca.efriesen.lydia_common.media.Song;
import ca.efriesen.lydia_phone.R;
import includes.Intents;

/**
 * User: eric
 * Date: 2013-06-08
 * Time: 10:16 AM
 */
public class MediaControlsFragment extends Fragment {

	private Activity activity;

	private static final String TAG = "Lydia Media Controls";

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setRetainInstance(true);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		this.activity = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.media_controls_fragment, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		activity.registerReceiver(updateMediaInfoReceiver, new IntentFilter(Intents.UPDATEMEDIAINFO));

		Button play = (Button) activity.findViewById(R.id.play);
		Button next = (Button) activity.findViewById(R.id.next);
		Button previous = (Button) activity.findViewById(R.id.previous);
		Button volDown = (Button) activity.findViewById(R.id.volume_down);
		Button volUp = (Button) activity.findViewById(R.id.volume_up);

		play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.sendBroadcast(new Intent(Intents.SENDMESSAGE).putExtra("key", "MEDIA").putExtra("value", Intents.PLAYPAUSE));
			}
		});
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.sendBroadcast(new Intent(Intents.SENDMESSAGE).putExtra("key", "MEDIA").putExtra("value", Intents.NEXT));
			}
		});
		previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.sendBroadcast(new Intent(Intents.SENDMESSAGE).putExtra("key", "MEDIA").putExtra("value", Intents.PREVIOUS));
			}
		});
		volDown.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.sendBroadcast(new Intent(Intents.SENDMESSAGE).putExtra("key", "VOLUME").putExtra("value", "DOWN"));
			}
		});
		volUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.sendBroadcast(new Intent(Intents.SENDMESSAGE).putExtra("key", "VOLUME").putExtra("value", "UP"));
			}
		});
	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			activity.unregisterReceiver(updateMediaInfoReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BroadcastReceiver updateMediaInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Log.d(TAG, "media controls");
				ImageView albumArt = (ImageView) activity.findViewById(R.id.album_art);
				Song song = (Song) intent.getSerializableExtra("ca.efriesen.Song");

				try {
//					BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), song.getAlbum().getAlbumArt());
//					// create a drawable from the bitmap, and set the background of the music button to the file
//					albumArt.setBackground(bitmapDrawable);
				} catch (Exception e) {
					e.printStackTrace();
				}

				TextView artistView = (TextView) activity.findViewById(R.id.artist);
				TextView titleView = (TextView) activity.findViewById(R.id.title);
				TextView albumView = (TextView) activity.findViewById(R.id.album);

				artistView.setText(song.getAlbum().getArtist().getName());
				titleView.setText(song.getName());
				albumView.setText(song.getAlbum().getName());
			} catch (Exception e) {
				Log.d(TAG, e.toString());
			}
		}
	};
}
