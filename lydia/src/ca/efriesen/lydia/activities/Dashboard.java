package ca.efriesen.lydia.activities;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.os.*;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.*;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.fragments.*;
import ca.efriesen.lydia.includes.Helpers;
import ca.efriesen.lydia.plugins.LastFM;
import ca.efriesen.lydia.services.HardwareManagerService;
import ca.efriesen.lydia_common.includes.Intents;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class Dashboard extends Activity {
	private static final String TAG = "DashboardActivity";
	private BluetoothAdapter mBluetoothAdapter = null;

	// plugins
	private LastFM lastFm;
	/**
	 * Called when the activities is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstance) {
		Log.d(TAG, "oncreate");
		super.onCreate(savedInstance);

		// set the entire view to a gesture overlay
//		GestureOverlayView overlayView = new GestureOverlayView(this);
		// inflate our xml
		View inflate = getLayoutInflater().inflate(R.layout.dashboard, null);
		// add the xml to the gesture overlay
//		overlayView.addView(inflate);
		// send all gestures to our listener
//		overlayView.addOnGesturePerformedListener(new GestureListener(this, getApplicationContext()));

//		overlayView.setGestureColor(Color.DKGRAY);
//		overlayView.setUncertainGestureColor(Color.TRANSPARENT);
		// set the content for the new overlay
//		setContentView(overlayView);
		setContentView(inflate);

		// start the hardware managerservice
		startService(new Intent(this, HardwareManagerService.class));

		// find the fragment container
		if (findViewById(R.id.home_screen_fragment) != null) {
			// if this is a resume, we'll have overlapping fragments
			if (savedInstance != null) {
				Log.d(TAG, "oncreate find view, return call");
				return;
			}
		}

		// add a listener to the back stack changed.  this allows for custom fragment callbacks
		getFragmentManager().addOnBackStackChangedListener(getListener());

		// initialize all plugins
		lastFm = new LastFM(getApplicationContext());

		Log.d(TAG, "oncreate finished");
	}

	@Override
	public void onResume() {
		super.onResume();

		checkGooglePlayServices();

		FragmentManager manager = getFragmentManager();

		final Fragment homeScreen = manager.findFragmentById(R.id.home_screen_fragment);
		final Fragment homeScreenTwo = manager.findFragmentById(R.id.home_screen_fragment_two);

		final Button homeScreenNext = (Button) findViewById(R.id.home_screen_next);
		final Button homeScreenPrev = (Button) findViewById(R.id.home_screen_previous);

		homeScreenNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().beginTransaction()
				.hide(homeScreen)
				.show(homeScreenTwo).commit();
			}
		});

		homeScreenPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().beginTransaction()
				.hide(homeScreenTwo).show(homeScreen).commit();
			}
		});

		// get bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			// Device doesn't support bluetooth
			finish();
			return;
		}

		// check if bt is on, and if not request it
//		if (!mBluetoothAdapter.isEnabled()) {
//			Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//			startActivityForResult(enableBt, 1);
//		}

		// bind to the hardware manager too
		bindService(new Intent(this, HardwareManagerService.class), hardwareServiceConnection, Context.BIND_AUTO_CREATE);

		// listen for battery broadcasts
		registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		// sms receiver listener
		registerReceiver(smsReceiver, new IntentFilter(Intents.SMSRECEIVED));
		// phone call listener
		registerReceiver(incomingCallReceiver, new IntentFilter(Intents.INCOMINGCALL));
	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			unregisterReceiver(mBatteryReceiver);
		} catch (Exception e) {
		e.printStackTrace();
		}

		try {
			unbindService(hardwareServiceConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			unregisterReceiver(smsReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			unregisterReceiver(incomingCallReceiver);
		} catch (Exception e) {
			Log.w(TAG, e);
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			lastFm.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
		.hide(fragmentManager.findFragmentById(R.id.map_container_fragment))
		.hide(fragmentManager.findFragmentById(R.id.settings_fragment))
		.addToBackStack(null)
		.commit();

		HomeScreenTwoFragment homeScreenTwoFragment = (HomeScreenTwoFragment) fragmentManager.findFragmentById(R.id.home_screen_fragment_two);
		MusicFragment musicFragment = (MusicFragment) fragmentManager.findFragmentById(R.id.music_fragment);
		MapContainerFragment mapContainerFragment = (MapContainerFragment) fragmentManager.findFragmentById(R.id.map_container_fragment);
		PhoneFragment phoneFragment = (PhoneFragment) fragmentManager.findFragmentById(R.id.phone_fragment);
		SettingsFragment settingsFragment = (SettingsFragment) fragmentManager.findFragmentById(R.id.settings_fragment);
		LauncherFragment launcherFragment = (LauncherFragment) fragmentManager.findFragmentById(R.id.launcher_fragment);

		// check all of our fragments for visibility, and execute the code contained within
		if (musicFragment.isVisible()) {
			musicFragment.onBackPressed();
		} else if (homeScreenTwoFragment.isVisible()) {
			homeScreenTwoFragment.onBackPressed();
		} else if (mapContainerFragment.isVisible()) {
			mapContainerFragment.onBackPressed();
		} else if (phoneFragment.isVisible()) {
			phoneFragment.onBackPressed();
		} else if (settingsFragment.isVisible()) {
			settingsFragment.onBackPressed();
		} else if(launcherFragment.isVisible()) {
			launcherFragment.onBackPressed();
		}
	}

/* ------------------ END OVERRIDES ------------------ */
/* ------------------ Start Broadcast Receivers and Service Connections ------------------ */

	private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		// get the level, and scale from the intent
		int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		// use "float" to do the math, since it will be decimals.  convert back to integer by *100, this adds a ".0" to the end
		float batteryPct = level / (float) scale * 100;
		// find the text view
		TextView battery = (TextView) findViewById(R.id.battery_pct);
		// get the value of in string form, and update the view
		battery.setText(String.valueOf((int)batteryPct) + "%");

		// add some color if the battery is low
		if ((int)batteryPct < 10) {
			battery.setTextColor(Color.RED);
		} else if ((int)batteryPct < 25) {
			battery.setTextColor(Color.YELLOW);
		} else {
			battery.setTextColor(Color.WHITE);
		}
		}
	};

	private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			final String phoneNumber = intent.getStringExtra("phoneNumber");
			final String message = intent.getStringExtra("message");

			final EditText reply = new EditText(Dashboard.this);

			new AlertDialog.Builder(
					Dashboard.this).setTitle(Helpers.getContactDisplayNameByNumber(getApplicationContext(), phoneNumber))
					.setMessage(message)
					.setView(reply)
					.setCancelable(false)
					.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							dialogInterface.cancel();
						}
					})
					.setPositiveButton(getString(R.string.reply), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
//							Log.d(TAG, replyMessage);
							sendBroadcast(new Intent(Intents.SMSREPLY).putExtra("message", reply.getText().toString()).putExtra("phoneNumber", phoneNumber));
							dialogInterface.cancel();
						}
					}).create().show();
		}
	};

	public BroadcastReceiver incomingCallReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			new AlertDialog.Builder(Dashboard.this).setTitle(getString(R.string.incoming_call))
				.setMessage(Helpers.getContactDisplayNameByNumber(getApplicationContext(), intent.getStringExtra("number")))
				.setCancelable(false)
				.setPositiveButton(getText(R.string.answer), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						// answer call
						dialogInterface.cancel();
					}
				})
				.setNegativeButton(getText(R.string.ignore), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						// ignore call
						dialogInterface.cancel();
					}
				}).create().show();
		}
	};


/* ------------------ End Broadcast Receivers and Service Connections ------------------ */
/* ------------------ Start View Updaters ------------------ */

//	// method to open the app drawer from the main menu
//	public void allApplications(View view) {
//		Intent allApps = new Intent(Intent.ACTION_MAIN);
//		allApps.setComponent(ComponentName.unflattenFromString("JakedUp.AppDrawer/.Main"));
//		allApps.addCategory(Intent.CATEGORY_LAUNCHER);
//		startActivity(allApps);
//	}

	public void contacts(View view) {
		Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
		while (phones.moveToNext()) {
			String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			Log.d("contacts", name + " " + number);
		}
	}

//	public void dialer(View view) {
//		final EditText editText = new EditText(getApplicationContext());
//		editText.setInputType(InputType.TYPE_CLASS_PHONE);
//		AlertDialog.Builder smsAlertBuilder = new AlertDialog.Builder(Dashboard.this);
//		smsAlertBuilder.setTitle("Make call")
//				.setView(editText)
//				.setCancelable(false)
//				.setPositiveButton(getString(R.string.dial), new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialogInterface, int i) {
//						String message = "PHONE" + BluetoothService.MESSAGE_DELIMETER + editText.getText().toString();
//						Log.d(TAG, "Phoning " + message);
//						bluetoothService.write(message.getBytes());
//						dialogInterface.cancel();
//					}
//				})
//				.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialogInterface, int i) {
//						dialogInterface.cancel();
//					}
//				});
//		AlertDialog smsAlert = smsAlertBuilder.create();
//		smsAlert.show();
//	}
//
	public void toggleBluetooth(View view) {
		sendBroadcast(new Intent(Intents.BLUETOOTHTOGGLE));
	}

/* ------------------ End View Updaters ------------------ */

	private ServiceConnection hardwareServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder iBinder) {
			// send the broadcast to the service to force a temperature update
			Intent updateTemp = new Intent(Intents.GETTEMPERATURE);
			sendBroadcast(updateTemp);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};


/* ------------------ Begin Fragment callbacks ------------------ */


	// listener for when the fragment backstack is changed.
	private FragmentManager.OnBackStackChangedListener getListener() {
		return new FragmentManager.OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() {
				// get the fragment manager
				FragmentManager manager = getFragmentManager();

				HomeScreenFragment homeScreenFragment = (HomeScreenFragment) manager.findFragmentById(R.id.home_screen_fragment);
				HomeScreenTwoFragment homeScreenTwoFragment = (HomeScreenTwoFragment) manager.findFragmentById(R.id.home_screen_fragment_two);
				MyMapFragment mapFragment = (MyMapFragment) manager.findFragmentById(R.id.map_fragment);
				MapContainerFragment mapContainerFragment = (MapContainerFragment) manager.findFragmentById(R.id.map_container_fragment);

				// for our homescreen fragment, if it's visible, execute the fragment visible callback
				if (homeScreenFragment.isVisible() || homeScreenTwoFragment.isVisible()) {
					homeScreenFragment.onFragmentVisible();
				} else {
					homeScreenFragment.onFragmentHidden();
				}

				if (mapContainerFragment.isVisible()) {
					mapFragment.onFragmentVisible();
				} else {
					mapFragment.onFragmentHidden();
				}
			}
		};
	}

/* ------------------ End Fragment callbacks ------------------ */
/* ------------------ Begin Google Play Service Check ------------------ */

	public void checkGooglePlayServices() {
		// check for proper version of google play services installed
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			// prompt to enable, or download/upgrade
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
			if (errorDialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				errorFragment.show(getFragmentManager(), "Geofence Detection");
			}
		}
	}

	public static class ErrorDialogFragment extends DialogFragment {
		private Dialog mDialog;

		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstance) {
			return mDialog;
		}
	}
}