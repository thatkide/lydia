package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.MusicSearch;
import ca.efriesen.lydia.activities.WebActivity;
import ca.efriesen.lydia_common.includes.Intents;
import ca.efriesen.lydia_common.media.Song;

/**
 * User: eric
 * Date: 2013-03-13
 * Time: 10:00 PM
 */
public class HomeScreenFragment extends Fragment {
	private PopupMenu musicPopup;

	private final static int RANDOM = 1;
	private final static int PLAYALL = 2;
	private final static int PLAYLISTS = 3;
	private final static int SEARCH = 4;

	private static final String TAG = "HomeScreen";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.home_screen_fragment, container, false);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		Activity activity = getActivity();
		// save the states of all the buttons on screen
		TextView defroster = (TextView) activity.findViewById(R.id.rear_window_defrost);
		savedInstanceState.putInt("defroster", defroster.getCurrentTextColor());

		TextView driverSeatHeat = (TextView) activity.findViewById(R.id.driver_seat_heat);
		savedInstanceState.putInt("driverSeatHeat", driverSeatHeat.getCurrentTextColor());

		TextView passengerSeatHeat = (TextView) activity.findViewById(R.id.passenger_seat_heat);
		savedInstanceState.putInt("passengerSeatHeat", passengerSeatHeat.getCurrentTextColor());

		TextView wiperToggle = (TextView) activity.findViewById(R.id.wiper_toggle);
		savedInstanceState.putInt("wiperToggle", wiperToggle.getCurrentTextColor());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Activity activity = getActivity();

		Button chrome = (Button) getActivity().findViewById(R.id.google);
		chrome.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent chrome = new Intent(Intent.ACTION_MAIN);
//				chrome.setComponent(ComponentName.unflattenFromString("com.android.chrome/.Main"));
//				chrome.addCategory(Intent.CATEGORY_LAUNCHER);
//				startActivity(chrome);

				startActivity(new Intent(getActivity(), WebActivity.class));
			}
		});

		// restore the button states
		if (savedInstanceState != null) {
			TextView defroster = (TextView) activity.findViewById(R.id.rear_window_defrost);
			defroster.setTextColor(savedInstanceState.getInt("defroster"));

			TextView driverSeatHeat = (TextView) activity.findViewById(R.id.driver_seat_heat);
			driverSeatHeat.setTextColor(savedInstanceState.getInt("driverSeatHeat"));

			TextView passengerSeatHeat = (TextView) activity.findViewById(R.id.passenger_seat_heat);
			passengerSeatHeat.setTextColor(savedInstanceState.getInt("passengerSeatHeat"));

			TextView wiperToggle = (TextView) activity.findViewById(R.id.wiper_toggle);
			wiperToggle.setTextColor(savedInstanceState.getInt("wiperToggle"));
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			getActivity().unregisterReceiver(updateMusicReceiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		final FragmentManager manager = getFragmentManager();
		final Activity activity = getActivity();

		final Fragment musicFragment = manager.findFragmentById(R.id.music_fragment);
		final Fragment homeScreen = manager.findFragmentById(R.id.home_screen_fragment);

		Button musicButton = (Button) activity.findViewById(R.id.music);
		musicButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				manager.beginTransaction()
				.hide(homeScreen)
				.show(musicFragment)
				.addToBackStack(null)
				.commit();
			}
		});

		// create the popup window for the music button
		musicPopup = new PopupMenu(activity.getApplicationContext(), activity.findViewById(R.id.music));
		musicPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case RANDOM: {
						Toast.makeText(getActivity().getApplicationContext(), getText(R.string.shuffle_all), Toast.LENGTH_SHORT).show();
						activity.sendBroadcast(new Intent(Intents.SHUFFLEALL));
						break;
					}
					case SEARCH: {
						startActivity(new Intent(getActivity(), MusicSearch.class));
						break;
					}
					case PLAYLISTS: {
					}
				}
				return false;
			}
		});
		// add(GroupID, ItemID, Order, Title
		musicPopup.getMenu().add(Menu.NONE, PLAYLISTS, Menu.NONE, R.string.playlists);
		musicPopup.getMenu().add(Menu.NONE, RANDOM, Menu.NONE, R.string.random);
		musicPopup.getMenu().add(Menu.NONE, SEARCH, Menu.NONE, R.string.search);

		musicButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// show the music popup window
				musicPopup.show();
				return true;
			}
		});

		activity.registerReceiver(updateMusicReceiver, new IntentFilter(Intents.UPDATEMEDIAINFO));

		Button phoneButton = (Button) activity.findViewById(R.id.phone_controls);
		phoneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				manager.beginTransaction()
				.hide(manager.findFragmentById(R.id.home_screen_fragment))
				.show(manager.findFragmentById(R.id.phone_fragment))
				.addToBackStack(null)
				.commit();
			}
		});

		Button allApps = (Button) activity.findViewById(R.id.all_applications);
		allApps.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				manager.beginTransaction()
				.hide(manager.findFragmentById(R.id.home_screen_fragment))
				.show(manager.findFragmentById(R.id.launcher_fragment))
				.addToBackStack(null)
				.commit();
			}
		});

//		Button statusButton = (Button) getActivity().findViewById(R.id.status);
//		statusButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Fragment statusFragment = new StatusFragment();
//				FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//				transaction.add(getId(), statusFragment, "status").commit();
//			}
//		});

//		SeekBar brightness = (SeekBar) getActivity().findViewById(R.id.brightness);
//		brightness.setMax(255);
//		brightness.setProgress(25);
//
//		brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//			@Override
//			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//				Intent updateBrightness = new Intent(HardwareManagerService.UPDATEBRIGHTNESS);
//				updateBrightness.putExtra("light", myIOIOService.TAILLIGHTBRIGHTNESS);
//				updateBrightness.putExtra("brightness", i);
//				getActivity().sendBroadcast(updateBrightness);
//			}
//
//			@Override
//			public void onStartTrackingTouch(SeekBar seekBar) {
//				//To change body of implemented methods use File | Settings | File Templates.
//			}
//
//			@Override
//			public void onStopTrackingTouch(SeekBar seekBar) {
//				//To change body of implemented methods use File | Settings | File Templates.
//			}
//		});

//		SeekBar brakeLights = (SeekBar) getActivity().findViewById(R.id.brake_lights);
//		brakeLights.setMax(255);
//		brakeLights.setProgress(50);
//		brakeLights.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//			@Override
//			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//				Intent updateBrightness = new Intent(HardwareManagerService.UPDATEBRIGHTNESS);
//				updateBrightness.putExtra("light", myIOIOService.BRAKELIGHTBRIGHTNESS);
//				updateBrightness.putExtra("brightness", i);
//				getActivity().sendBroadcast(updateBrightness);
//			}
//
//			@Override
//			public void onStartTrackingTouch(SeekBar seekBar) {
//				//To change body of implemented methods use File | Settings | File Templates.
//			}
//
//			@Override
//			public void onStopTrackingTouch(SeekBar seekBar) {
//				//To change body of implemented methods use File | Settings | File Templates.
//			}
//		});
//
//		SeekBar signalLighs = (SeekBar) getActivity().findViewById(R.id.signal_lights);
//		signalLighs.setMax(255);
//		brakeLights.setProgress(50);
//		brakeLights.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//			@Override
//			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//				Intent updateBrightness = new Intent(HardwareManagerService.UPDATEBRIGHTNESS);
//				updateBrightness.putExtra("light", myIOIOService.SIGNALLIGHTBRIGHTNESS);
//				updateBrightness.putExtra("brightness", i);
//				getActivity().sendBroadcast(updateBrightness);
//			}
//
//			@Override
//			public void onStartTrackingTouch(SeekBar seekBar) {
//				//To change body of implemented methods use File | Settings | File Templates.
//			}
//
//			@Override
//			public void onStopTrackingTouch(SeekBar seekBar) {
//				//To change body of implemented methods use File | Settings | File Templates.
//			}
//		});
	}

	public void onFragmentVisible() {
		// when the back button is pressed, make sure the home screen next and prev buttons are shown.
		Activity activity = getActivity();
		Button homescreenNext = (Button) activity.findViewById(R.id.home_screen_next);
		Button homescreenPrev = (Button) activity.findViewById(R.id.home_screen_previous);

		homescreenNext.setVisibility(View.VISIBLE);
		homescreenPrev.setVisibility(View.VISIBLE);
	}

	public void onFragmentHidden() {
		// when the back button is pressed, make sure the home screen next and prev buttons are hidden.
		Activity activity = getActivity();
		Button homescreenNext = (Button) activity.findViewById(R.id.home_screen_next);
		Button homescreenPrev = (Button) activity.findViewById(R.id.home_screen_previous);

		homescreenNext.setVisibility(View.GONE);
		homescreenPrev.setVisibility(View.GONE);
	}

	private BroadcastReceiver updateMusicReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Song song = (Song) intent.getSerializableExtra("ca.efriesen.Song");

			// find the music button on the home screen
			Button music = (Button) getActivity().findViewById(R.id.music);
			// set the background of the button to the album art
			try {
				BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), song.getAlbum().getAlbumArt(getActivity().getApplicationContext()));

				// create a drawable from the bitmap, and set the background of the music button to the file
				music.setBackground(bitmapDrawable);
				// remove the record image
				music.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
				// remove the text on the button
				music.setText("");
			} catch (Exception e) {
				music.setBackgroundResource(R.drawable.button_bg);
				music.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.vinyl, 0, 0);
				music.setText(R.string.music);
			}
		}
	};
}
