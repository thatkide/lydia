package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.*;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.Dashboard;
import ca.efriesen.lydia.activities.MusicSearch;
import ca.efriesen.lydia.activities.WebActivity;
import ca.efriesen.lydia.controllers.ButtonController;
import ca.efriesen.lydia.controllers.ButtonControllers.*;
import ca.efriesen.lydia.services.MediaService;
import ca.efriesen.lydia_common.media.Album;
import ca.efriesen.lydia_common.media.Song;

import java.util.ArrayList;

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

	private LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		setRetainInstance(true);
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
		localBroadcastManager.registerReceiver(updateMusicReceiver, new IntentFilter(MediaService.UPDATE_MEDIA_INFO));

		int numButtons = 6;

		for (int i=0; i<numButtons; i++) {
			// get the resource id for the button
			int resId = getResources().getIdentifier("home" + i, "id", getActivity().getPackageName());
			// get the button
			Button button = (Button) getActivity().findViewById(resId);
			button.setOnClickListener(new ButtonController(getActivity()));
		}

		Bundle navBundle = new Bundle();
		navBundle.putString("title", getString(R.string.navigation));
		navBundle.putString("drawable", "compass");
		navBundle.putString("action", NavigationButton.ACTION);

		Bundle musicBundle = new Bundle();
		musicBundle.putString("title", getString(R.string.music));
		musicBundle.putString("drawable", "vinyl");
		musicBundle.putString("action", MusicButton.ACTION);

		Bundle airRideBundle = new Bundle();
		airRideBundle.putString("title", getString(R.string.air_ride));
		airRideBundle.putString("drawable", "jet_engine");
		airRideBundle.putString("action", AirRideButton.ACTION);

		Bundle phoneBundle = new Bundle();
		phoneBundle.putString("title", getString(R.string.phone));
		phoneBundle.putString("drawable", "phone");
		phoneBundle.putString("action", PhoneButton.ACTION);

		Bundle androidBundle = new Bundle();
		androidBundle.putString("title", getString(R.string.all_apps));
		androidBundle.putString("drawable", "android");
		androidBundle.putString("action", AndroidButton.ACTION);

		Bundle chromeBundle = new Bundle();
		chromeBundle.putString("title", getString(R.string.chrome));
		chromeBundle.putString("drawable", "chrome");
		chromeBundle.putString("action", ChromeButton.ACTION);

		ArrayList<Bundle> buttons = new ArrayList<Bundle>();
		buttons.add(navBundle);
		buttons.add(musicBundle);
		buttons.add(airRideBundle);
		buttons.add(phoneBundle);
		buttons.add(androidBundle);
		buttons.add(chromeBundle);

		// loop over all buttons
		for (int i=0; i<buttons.size(); i++) {
			// get the bundle of info
			Bundle bundle = buttons.get(i);
			// get the resource id for the button
			int resId = getResources().getIdentifier("home" + i, "id", getActivity().getPackageName());
			// get the button
			Button button = (Button) getActivity().findViewById(resId);
			// set the text to the proper title
			button.setText(bundle.getString("title"));
			// get the image resource id
			int imgId = getResources().getIdentifier(bundle.getString("drawable"), "drawable", getActivity().getPackageName());
			// get the drawable
			Drawable img = getActivity().getResources().getDrawable(imgId);
			// set it to the top on the button
			button.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
			button.setTag(bundle);
		}

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

//		// create the popup window for the music button
//		musicPopup = new PopupMenu(activity.getApplicationContext(), activity.findViewById(R.id.home1));
//		musicPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//			@Override
//			public boolean onMenuItemClick(MenuItem item) {
//				switch (item.getItemId()) {
//					case RANDOM: {
//						Toast.makeText(getActivity().getApplicationContext(), getText(R.string.shuffle_all), Toast.LENGTH_SHORT).show();
////						activity.sendBroadcast(new Intent(Intents.SHUFFLEALL));
//						break;
//					}
//					case SEARCH: {
//						startActivity(new Intent(getActivity(), MusicSearch.class));
//						break;
//					}
//					case PLAYLISTS: {
//					}
//				}
//				return false;
//			}
//		});
//		// add(GroupID, ItemID, Order, Title
//		musicPopup.getMenu().add(Menu.NONE, PLAYLISTS, Menu.NONE, R.string.playlists);
//		musicPopup.getMenu().add(Menu.NONE, RANDOM, Menu.NONE, R.string.random);
//		musicPopup.getMenu().add(Menu.NONE, SEARCH, Menu.NONE, R.string.search);
//
//		musicButton.setOnLongClickListener(new View.OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				// show the music popup window
//				musicPopup.show();
//				return true;
//			}
//		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			localBroadcastManager.unregisterReceiver(updateMusicReceiver);
		} catch (IllegalArgumentException e) { }
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		localBroadcastManager.sendBroadcast(new Intent(MediaService.GET_CURRENT_SONG));
	}

	private BroadcastReceiver updateMusicReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Song song = (Song) intent.getSerializableExtra(MediaService.SONG);
			Album album = song.getAlbum();
			try {
				// find the music button on the home screen
				Button music = (Button) getActivity().findViewById(R.id.home1);
				try {
					// save the album id
					PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt("currentAlbum", album.getId()).commit();
					// set the background of the button to the album art

					Bitmap bitmap = album.getAlbumArt(getActivity().getApplicationContext());

					if (bitmap != null) {
						BitmapDrawable bitmapDrawable = new BitmapDrawable(getActivity().getResources(), bitmap);
						// create a drawable from the bitmap, and set the background of the music button to the file
						music.setBackground(bitmapDrawable);
						// remove the record image
						music.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
						// remove the text on the button
						music.setText("");
					} else {
						music.setBackgroundResource(R.drawable.button_bg);
						music.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.vinyl, 0, 0);
						music.setText(R.string.music);
					}
				} catch (Exception e) {
					music.setBackgroundResource(R.drawable.button_bg);
					music.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.vinyl, 0, 0);
					music.setText(R.string.music);
				}
			} catch (Exception e) {}
		}
	};

}
