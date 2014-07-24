package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.callbacks.FragmentAnimationCallback;
import ca.efriesen.lydia.devices.Master;
import ca.efriesen.lydia_common.BluetoothService;
import ca.efriesen.lydia_common.includes.Intents;


/**
 * User: eric
 * Date: 2012-08-29
 * Time: 10:17 PM
 */
public class FooterFragment extends Fragment implements FragmentAnimationCallback, View.OnClickListener {
	private static final String TAG = FooterFragment.class.getSimpleName();

	private Activity activity;

	// setup the audio manager and seek bar vars
	private AudioManager mAudioManager;
	private SeekBar volumeControl;
	private int maxVolume, currentVolume;

	private int volRefreshTime = 100;
	private Handler volUpHandler = new Handler();
	private Runnable volUpRunnable = new Runnable() {
		@Override
		public void run() {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + 1, 0);
			volUpHandler.postAtTime(this, SystemClock.uptimeMillis() + volRefreshTime);
		}
	};

	private Handler volDownHandler = new Handler();
	private Runnable volDownRunnable = new Runnable() {
		@Override
		public void run() {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) - 1, 0);
			volDownHandler.postAtTime(this, SystemClock.uptimeMillis() + volRefreshTime);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
		return inflater.inflate(R.layout.footer_fragment, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		activity = getActivity();

		// get all of our controls
		ImageButton volDown = (ImageButton) activity.findViewById(R.id.volume_down);
		ImageButton volUp = (ImageButton) activity.findViewById(R.id.volume_up);

		ImageButton home = (ImageButton) activity.findViewById(R.id.home);
		ImageButton back = (ImageButton) activity.findViewById(R.id.back);

		home.setOnClickListener(this);

		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onBackPressed();
			}
		});

		// volume down button
		volDown.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
					volDownHandler.removeCallbacks(volDownRunnable);
					volDownHandler.postAtTime(volDownRunnable, SystemClock.uptimeMillis() + volRefreshTime);
				} else if (MotionEvent.ACTION_UP == motionEvent.getAction()) {
					volDownHandler.removeCallbacks(volDownRunnable);
				}
				return true;
			}
		});

		// volume up button
		volUp.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
					volUpHandler.removeCallbacks(volUpRunnable);
					volUpHandler.postAtTime(volUpRunnable, SystemClock.uptimeMillis() + volRefreshTime);
				} else if (MotionEvent.ACTION_UP == motionEvent.getAction()) {
					volUpHandler.removeCallbacks(volUpRunnable);
				}
				return true;
			}
		});

		volumeControl = (SeekBar) activity.findViewById(R.id.volume_control);
		// setup the audio manager from the main activity
		mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
		// volume control
		maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		// set the constraints
		volumeControl.setMax(maxVolume);
		volumeControl.setProgress(currentVolume);

		volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
				// find our mute button image
				ImageButton mute = (ImageButton) activity.findViewById(R.id.mute);
				// change the image depending on the current volume level
				if (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0) {
					mute.setImageResource(R.drawable.device_access_volume_on);
				} else {
					mute.setImageResource(R.drawable.device_access_volume_muted);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});


		// filter for volume changed intent fired by the hardware buttons
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.media.VOLUME_CHANGED_ACTION");
		// register the receiver to the main activity
		activity.registerReceiver(mReceiver, filter);

		// register the light and temperature receivers
		activity.registerReceiver(mLightSensorReceiver, new IntentFilter(Master.LIGHTVALUE));
		activity.registerReceiver(insideTemperatureReceiver, new IntentFilter(Master.INSIDETEMPERATURE));
		activity.registerReceiver(outsideTemperatureReceiver, new IntentFilter(Master.OUTSIDETEMPERATURE));

		// bluetooth stuff
		activity.registerReceiver(bluetoothManager, new IntentFilter(Intents.BLUETOOTHMANAGER));

		// listen for battery broadcasts
		activity.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			getActivity().unregisterReceiver(mLightSensorReceiver);
		} catch (Exception e) {}
	}

	@Override
	public void onStop() {
		super.onStop();
		Activity activity = getActivity();
		try {
			activity.unregisterReceiver(mReceiver);
		} catch (IllegalArgumentException e) {}
		try {
			activity.unregisterReceiver(outsideTemperatureReceiver);
		} catch (IllegalArgumentException e) {}
		try {
			activity.unregisterReceiver(insideTemperatureReceiver);
		} catch (IllegalArgumentException e) {}
		try {
			activity.unregisterReceiver(bluetoothManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			activity.unregisterReceiver(mBatteryReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// get the broadcast and update our ui
	private BroadcastReceiver insideTemperatureReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
//			TextView insideTemp = (TextView) getActivity().findViewById(R.id.inside_temperature);
//			insideTemp.setText("Inside: " + intent.getStringExtra(Master.INSIDETEMPERATURE) + "\u2103");
		}
	};

	private BroadcastReceiver outsideTemperatureReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
//			TextView outsideTemp = (TextView) getActivity().findViewById(R.id.outside_temperature);
//			outsideTemp.setText("Outside: " + intent.getStringExtra(Master.OUTSIDETEMPERATURE) + "\u2103");
		}
	};


	// receiver method
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// bit of a hack, but it works
			int volume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0);
			// update the slider to the new volume
			volumeControl.setProgress(volume);
		}
	};


	private BroadcastReceiver mLightSensorReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Activity activity = getActivity();
			int brightness = Integer.parseInt(intent.getStringExtra(Master.LIGHTVALUE));
//			ImageView dayNight = (ImageView) activity.findViewById(R.id.day_night);

			// get the values store in the preferences
			int lowerLevel = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(activity).getString("minLight", "0"));
			int upperLevel = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(activity).getString("maxLight", "0"));

			// if the brightness is less than the lower level, turn to night
			if (brightness < lowerLevel) {
//				dayNight.setImageResource(R.drawable.device_access_brightness_low);
				// if the brightness is above the upper level, switch to day
			} else if (brightness > upperLevel) {
//				dayNight.setImageResource(R.drawable.device_access_brightness_high);
			}
			// else, the brightness is between, but has not passed a threshold, so do nothing
		}
	};

	private BroadcastReceiver bluetoothManager = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
//			ImageButton btImage = (ImageButton) getActivity().findViewById(R.id.bluetooth);
			BluetoothDevice device = intent.getParcelableExtra("device");
			int state = intent.getIntExtra("state", 0);
			switch (state) {
				case BluetoothService.STATE_CONNECTED: {
					try {
						Toast.makeText(getActivity(), "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
					} catch (Exception e) {}
//					btImage.setImageResource(R.drawable.device_access_bluetooth_connected);
					break;
				}
				case BluetoothService.STATE_NONE: {
//					btImage.setImageResource(R.drawable.device_access_bluetooth);
					break;
				}
			}
		}
	};

	private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// get the level, and scale from the intent
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

			// use "float" to do the math, since it will be decimals.  convert back to integer by *100, this adds a ".0" to the end
			float batteryPct = level / (float) scale * 100;
//		find the text view
//			TextView battery = (TextView) getActivity().findViewById(R.id.battery_pct);
//		get the value of in string form, and update the view
//			battery.setText(String.valueOf((int)batteryPct) + "%");

//		add some color if the battery is low
			if ((int)batteryPct < 10) {
//				battery.setTextColor(Color.RED);
			} else if ((int)batteryPct < 25) {
//				battery.setTextColor(Color.YELLOW);
			} else {
//				battery.setTextColor(Color.WHITE);
			}
		}
	};

	// this only gets called if the passenger controls is gone
	@Override
	public void animationComplete(int direction) {
		FragmentManager manager = getFragmentManager();
		manager.beginTransaction()
				.setCustomAnimations(R.anim.container_slide_in_down, R.anim.container_slide_out_down)
				.replace(R.id.home_screen_fragment, new HomeScreenFragment())
				.commit();

		if (((DriverControlsFragment)manager.findFragmentById(R.id.driver_controls)).getGroup() != BaseButton.GROUP_USER) {
			manager.beginTransaction()
				.setCustomAnimations(R.anim.container_slide_in_down, R.anim.container_slide_out_down)
				.replace(R.id.driver_controls, new DriverControlsFragment())
				.commit();
		}
	}

	@Override
	public void animationStart(int direction) { }

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.home) {
			if (!(getFragmentManager().findFragmentById(R.id.home_screen_fragment) instanceof HomeScreenFragment)) {
				View driverView = activity.findViewById(R.id.driver_controls);
				View passengerView = activity.findViewById(R.id.passenger_controls);

				final FragmentManager manager = getFragmentManager();

				// both sides are in view, just replace the home screen
				if (driverView.getVisibility() == View.VISIBLE && passengerView.getVisibility() == View.VISIBLE) {
					manager.beginTransaction()
							.setCustomAnimations(R.anim.container_slide_in_down, R.anim.container_slide_out_down)
							.replace(R.id.home_screen_fragment, new HomeScreenFragment())
							.commit();
				}

				// if the passenger controls is hidden, animate it in and do the home slide up after
				if (passengerView.getVisibility() == View.GONE) {
					((PassengerControlsFragment) manager.findFragmentById(R.id.passenger_controls)).showFragment(this);
				}
				// if the driver controls is hidden, animate it in and do the home slide up after
				if (driverView.getVisibility() == View.GONE) {
					((DriverControlsFragment) manager.findFragmentById(R.id.driver_controls)).showFragment(new FragmentAnimationCallback() {
						@Override
						public void animationComplete(int direction) {
							activity.findViewById(R.id.driver_controls).setVisibility(View.VISIBLE);
						}

						@Override
						public void animationStart(int direction) {
							// Load new driver controls with nav buttons
							DriverControlsFragment driverControlsFragment = new DriverControlsFragment();
							Bundle args = new Bundle();
							args.putInt("group", BaseButton.GROUP_USER);
							driverControlsFragment.setArguments(args);

							manager.beginTransaction()
									.replace(R.id.driver_controls, driverControlsFragment)
									.commit();
						}
					});
				}

			}
		}
	}

}
	