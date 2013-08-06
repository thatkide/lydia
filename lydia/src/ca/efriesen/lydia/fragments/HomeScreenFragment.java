package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.Dashboard;
import ca.efriesen.lydia.activities.MusicSearch;
import ca.efriesen.lydia.activities.WebActivity;
import ca.efriesen.lydia.services.MediaService;
import ca.efriesen.lydia_common.includes.Intents;
import ca.efriesen.lydia_common.media.Album;
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

	private static final String TAG = "lydia HomeScreen";

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		getActivity().registerReceiver(updateMusicReceiver, new IntentFilter(Intents.UPDATEMEDIAINFO));
		// bind to the media service
		// we use this to update the icon on the home screen
		getActivity().bindService(new Intent(getActivity(), MediaService.class), mediaServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.home_screen_fragment, container, false);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		Activity activity = getActivity();
		try {
			TextView driverSeatHeat = (TextView) activity.findViewById(R.id.driver_seat_heat);
			savedInstanceState.putInt("driverSeatHeat", driverSeatHeat.getCurrentTextColor());

			TextView passengerSeatHeat = (TextView) activity.findViewById(R.id.passenger_seat_heat);
			savedInstanceState.putInt("passengerSeatHeat", passengerSeatHeat.getCurrentTextColor());

			TextView wiperToggle = (TextView) activity.findViewById(R.id.wiper_toggle);
			savedInstanceState.putInt("wiperToggle", wiperToggle.getCurrentTextColor());
		} catch (Exception e) {}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final FragmentManager manager = getFragmentManager();
		final Activity activity = getActivity();

		final Button homeScreenNext = (Button) activity.findViewById(R.id.home_screen_next);
		final Button homeScreenPrev = (Button) activity.findViewById(R.id.home_screen_previous);

		homeScreenNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.controls_slide_in_left, R.anim.controls_slide_in_right)
						.replace(R.id.home_screen_fragment, new HomeScreenTwoFragment(), "homeScreenFragment")
						.addToBackStack(null)
						.commit();
				((Dashboard)getActivity()).setHomeScreenClass(HomeScreenTwoFragment.class);
			}
		});

		homeScreenPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.controls_slide_out_left, R.anim.controls_slide_out_right)
						.replace(R.id.home_screen_fragment, new HomeScreenTwoFragment(), "homeScreenFragment")
						.addToBackStack(null)
						.commit();
				((Dashboard)getActivity()).setHomeScreenClass(HomeScreenTwoFragment.class);
			}
		});

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

		// map on the homescreen that opens the map fragment
		Button map = (Button) getActivity().findViewById(R.id.navigation);
		map.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				manager.beginTransaction()
						.setCustomAnimations(R.anim.container_slide_out_up, R.anim.container_slide_in_up)
						.replace(R.id.home_screen_container_fragment, new MapContainerFragment(), "homeScreenContainerFragment")
						.addToBackStack(null)
						.commit();
				((Dashboard)getActivity()).setHomeScreenClass(HomeScreenFragment.class);
			}
		});

		Button musicButton = (Button) activity.findViewById(R.id.music);
		musicButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MusicFragment musicFragment = new MusicFragment();
				manager.beginTransaction()
						.setCustomAnimations(R.anim.homescreen_slide_out_up, R.anim.homescreen_slide_in_up)
						.replace(R.id.home_screen_fragment, musicFragment, "musicFragment")
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
//						activity.sendBroadcast(new Intent(Intents.SHUFFLEALL));
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

		Button phoneButton = (Button) activity.findViewById(R.id.phone_controls);
		phoneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				manager.beginTransaction()
						.setCustomAnimations(R.anim.homescreen_slide_out_up, R.anim.homescreen_slide_in_up)
						.replace(R.id.home_screen_fragment, new PhoneFragment(), "phoneFragment")
						.addToBackStack(null)
						.commit();
				((Dashboard)getActivity()).setHomeScreenClass(HomeScreenFragment.class);
			}
		});

		Button allApps = (Button) activity.findViewById(R.id.all_applications);
		allApps.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				manager.beginTransaction()
						.setCustomAnimations(R.anim.homescreen_slide_out_up, R.anim.homescreen_slide_in_up)
						.replace(R.id.home_screen_fragment, new LauncherFragment(), "launcherFragment")
						.addToBackStack(null)
						.commit();
				((Dashboard)getActivity()).setHomeScreenClass(HomeScreenFragment.class);
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			getActivity().unregisterReceiver(updateMusicReceiver);
		} catch (IllegalArgumentException e) { }
		try {
			getActivity().unbindService(mediaServiceConnection);
		} catch (Exception e) { }
	}

	@Override
	public void onStart() {
		super.onStart();

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

	@Override
	public void onResume() {
		super.onResume();
	}

	private void setAlbumArtImage(Album album) {
		try {
			// find the music button on the home screen
			Button music = (Button) getActivity().findViewById(R.id.music);
			try {
				// save the album id
				PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt("currentAlbum", album.getId()).commit();
				// set the background of the button to the album art

				BitmapDrawable bitmapDrawable = new BitmapDrawable(getActivity().getResources(), album.getAlbumArt(getActivity().getApplicationContext()));

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
		} catch (Exception e) {}
	}

	private BroadcastReceiver updateMusicReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Song song = (Song) intent.getSerializableExtra("ca.efriesen.Song");
			setAlbumArtImage(song.getAlbum());
		}
	};

	private ServiceConnection mediaServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder iBinder) {
			MediaService mediaService = ((MediaService.MediaServiceBinder) iBinder).getService();
			if (mediaService.getState() != MediaService.State.Dead) {
				Album album = new Album();
				album.setId(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("currentAlbum", 0));
				setAlbumArtImage(album);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) { }
	};

}
