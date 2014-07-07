package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.*;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.callbacks.FragmentAnimationCallback;
import ca.efriesen.lydia.fragments.NotificationFragments.MusicNotificationFragment;
import ca.efriesen.lydia.services.MediaService;


/**
 * User: eric
 * Date: 2012-10-06
 * Time: 10:31 AM
 */
public class HeaderFragment extends Fragment implements FragmentAnimationCallback, View.OnClickListener { //implements View.OnTouchListener {

	public static final String TAG = "Header Fragment";

	private Activity activity;
	private LocalBroadcastManager localBroadcastManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.header_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		activity = getActivity();
		localBroadcastManager = LocalBroadcastManager.getInstance(activity);

		getFragmentManager().beginTransaction()
				.replace(R.id.notification_bar, new MusicNotificationFragment()).commit();
	}

	@Override
	public void onStart() {
		super.onStart();

		// get all the buttons
		ImageButton home = (ImageButton) activity.findViewById(R.id.home);
		ImageButton playPause = (ImageButton) activity.findViewById(R.id.play_pause);
		ImageButton previous = (ImageButton) activity.findViewById(R.id.previous);
		ImageButton next = (ImageButton) activity.findViewById(R.id.next);


		home.setOnClickListener(this);

		playPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				localBroadcastManager.sendBroadcast(new Intent(MediaService.MEDIA_COMMAND).putExtra(MediaService.MEDIA_COMMAND, MediaService.PLAY_PAUSE));
			}
		});

		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				localBroadcastManager.sendBroadcast(new Intent(MediaService.MEDIA_COMMAND).putExtra(MediaService.MEDIA_COMMAND, MediaService.NEXT));
			}
		});

		previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				localBroadcastManager.sendBroadcast(new Intent(MediaService.MEDIA_COMMAND).putExtra(MediaService.MEDIA_COMMAND, MediaService.PREVIOUS));
			}
		});
	}

	@Override
	public void animationComplete(int direction) {
		FragmentManager manager = getFragmentManager();
		manager.beginTransaction()
				.setCustomAnimations(R.anim.container_slide_in_down, R.anim.container_slide_out_down)
				.replace(R.id.home_screen_fragment, new HomeScreenFragment())
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.home) {
			if (!(getFragmentManager().findFragmentById(R.id.home_screen_fragment) instanceof HomeScreenFragment)) {
				// if the passenger controls is hidden, animate it in and do the home slide up after
				if (activity.findViewById(R.id.passenger_controls).getVisibility() == View.GONE) {
					activity.findViewById(R.id.passenger_controls).setVisibility(View.VISIBLE);
					((PassengerControlsFragment) getFragmentManager().findFragmentById(R.id.passenger_controls)).showFragment(this);
				// if it's already here, just show home
				} else {
					getFragmentManager().beginTransaction()
							.setCustomAnimations(R.anim.container_slide_in_down, R.anim.container_slide_out_down)
							.replace(R.id.home_screen_fragment, new HomeScreenFragment())
							.addToBackStack(null)
							.commit();
				}
			}
		}
	}
}