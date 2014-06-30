package ca.efriesen.lydia.activities;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.*;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.*;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.services.ArduinoService;
import ca.efriesen.lydia.fragments.*;
import ca.efriesen.lydia.fragments.Settings.SystemSettingsFragment;
import ca.efriesen.lydia.plugins.LastFM;
import ca.efriesen.lydia.services.HardwareManagerService;
import ca.efriesen.lydia.services.MediaService;
import ca.efriesen.lydia_common.includes.Intents;
import com.appaholics.updatechecker.UpdateChecker;
import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.Observable;
import java.util.Observer;

public class Dashboard extends Activity {
	private static final String TAG = "accessory";//lydia Dashboard Activity";
	private BluetoothAdapter mBluetoothAdapter = null;


	// plugins
	private LastFM lastFm;

	private Class driverControlsClass;
	private Class passengerControlsClass;

	//the first part of this string have to be the package name
	private static final String ACTION_USB_PERMISSION = "ca.efriesen.lydia.action.USB_PERMISSION";
	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;
	private UsbAccessory mUsbAccessory;

	/**
	 * Called when the activities is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);

		final UpdateChecker checker = new UpdateChecker(this, true);
		checker.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object o) {
				if (checker.isUpdateAvailable()) {
					checker.downloadAndInstall(getString(R.string.update_apk_url));
				}
			}
		});

//		checker.checkForUpdateByVersionCode(getString(R.string.update_url));

		// don't include bug sense it the key hasn't been changed
		if (!getString(R.string.bugsenseApiKey).equalsIgnoreCase("Your Bugsense Key")) {
//			BugSenseHandler.initAndStartSession(Dashboard.this, getString(R.string.bugsenseApiKey));
		}
		setContentView(R.layout.dashboard);

		// start the hardware managerservice
		startService(new Intent(this, HardwareManagerService.class));

		// start the media service
		startService(new Intent(this, MediaService.class));

		// find the fragment container
		// if this is a resume, we'll have overlapping fragments
		if (getFragmentManager().findFragmentByTag("homeScreenContainerFragment") == null) {
			getFragmentManager().beginTransaction()
				.replace(R.id.header_fragment, new HeaderFragment())
				.replace(R.id.home_screen_container_fragment, new HomeScreenContainerFragment(), "homeScreenContainerFragment")
				.replace(R.id.footer_fragment, new FooterFragment())
				.commit();
		}

		// initialize all plugins
		lastFm = new LastFM(this);

		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);
	}

	@Override
	public void onResume() {
		super.onResume();

		checkGooglePlayServices();

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

		// get list of accessories
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		// get first accessory
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);

		if (mUsbAccessory != null) {
			Log.d(TAG, "stop service");
			stopService(new Intent(this, ArduinoService.class));
		}

		// if we got a valid accessory
		if (accessory != null) {
			mUsbAccessory = accessory;
			// if we have permission
			if (mUsbManager.hasPermission(accessory)) {
				Log.d(TAG, "starting in onresume");
				// start the arduino service
				Intent i = new Intent(this, ArduinoService.class);
				i.putExtra(UsbManager.EXTRA_ACCESSORY, accessory);
				startService(i);
			// otherwise ask for permission
			} else {
				synchronized (mUsbReceiver) {
					if (mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory, mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
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
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		try {
			lastFm.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			unregisterReceiver(mUsbReceiver);
		} catch (Exception e) {}
	}

	public void setDriverControlsClass(Class driverControlsClass) {
		this.driverControlsClass = driverControlsClass;
	}

	public void setPassengerControlsClass(Class passengerControlsClass) {
		this.passengerControlsClass = passengerControlsClass;
	}

	@Override
	public void onBackPressed() {
		MusicFragment musicFragment = (MusicFragment) getFragmentManager().findFragmentByTag("musicFragment");
		// get the generic home screen fragment from the tag
		Fragment homeScreenFragment = getFragmentManager().findFragmentByTag("homeScreenFragment");
		Fragment driverControls = getFragmentManager().findFragmentByTag("driverControls");
		Fragment settingsFragment = getFragmentManager().findFragmentById(R.id.settings_fragment);
		MyMapFragment mapFragment = (MyMapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);

		// music fragment has special handling, check it first,
		if (musicFragment != null && musicFragment.isVisible()) {
			musicFragment.onBackPressed();
			return;
		} else if (mapFragment != null && mapFragment.isVisible()) {
			// check to see if the map has handled the back press, if not, we do it
			if (!mapFragment.onBackPressed()) {
				super.onBackPressed();
				return;
			}
		} else if(settingsFragment != null && settingsFragment.isVisible()) {
			// check if the current settings fragment is an instance of the system settings (the one displayed first), and if not, do super.onbackpressed and return.  if it is, replace the home screen like any other fragment
			if (!(settingsFragment instanceof SystemSettingsFragment)) {
				super.onBackPressed();
				return;
			}
		}

		// if the controls fragment is visible, only replace the center portion
		if (driverControls.isVisible() && !homeScreenFragment.isVisible()) {
			try {
				getFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.homescreen_slide_in_down, R.anim.homescreen_slide_out_down)
						.replace(R.id.home_screen_fragment, new HomeScreenFragment(), "homeScreenFragment")
						.commit();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

		// check if the home screen is visible, if not, go back, passing in the class of the home screen we want (one, two, etc...)
		} else if (!homeScreenFragment.isVisible()) {
			getFragmentManager().beginTransaction()
					.setCustomAnimations(R.anim.container_slide_in_down, R.anim.container_slide_out_down)
					.replace(R.id.home_screen_container_fragment, new HomeScreenContainerFragment(driverControlsClass, passengerControlsClass), "homeScreenContainerFragment") // pass in the selected home screen
					.commit();
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
//		find the text view
		TextView battery = (TextView) findViewById(R.id.battery_pct);
//		get the value of in string form, and update the view
		battery.setText(String.valueOf((int)batteryPct) + "%");

//		add some color if the battery is low
		if ((int)batteryPct < 10) {
			battery.setTextColor(Color.RED);
		} else if ((int)batteryPct < 25) {
			battery.setTextColor(Color.YELLOW);
		} else {
			battery.setTextColor(Color.WHITE);
		}
		}
	};



/* ------------------ End Broadcast Receivers and Service Connections ------------------ */
/* ------------------ Start View Updaters ------------------ */
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

	// Receiver for the USB intents
	// this is fired on attach and permission granted
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// check if permission intent
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					// if permission was granted
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						Log.d(TAG, "starting from broadcast receiver");
						// start the service and pass the intent
						Intent i = new Intent(getApplicationContext(), ArduinoService.class);
						i.putExtras(intent);
						startService(i);
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {

			}
		}
	};

}