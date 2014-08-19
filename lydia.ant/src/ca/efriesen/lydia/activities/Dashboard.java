package ca.efriesen.lydia.activities;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.database.Cursor;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Color;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.*;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.callbacks.FragmentOnBackPressedCallback;
import ca.efriesen.lydia.controllers.BackgroundController;
import ca.efriesen.lydia.controllers.Controller;
import ca.efriesen.lydia.controllers.NotificationController;
import ca.efriesen.lydia.databases.ButtonConfigDataSource;
import ca.efriesen.lydia.fragments.NotificationFragments.MusicNotificationFragment;
import ca.efriesen.lydia.fragments.NotificationFragments.SystemNotificationFragment;
import ca.efriesen.lydia.interfaces.NotificationInterface;
import ca.efriesen.lydia.services.ArduinoService;
import ca.efriesen.lydia.fragments.*;
import ca.efriesen.lydia.plugins.LastFM;
import ca.efriesen.lydia.services.HardwareManagerService;
import ca.efriesen.lydia.services.MediaService;
import ca.efriesen.lydia_common.includes.Intents;
import com.appaholics.updatechecker.UpdateChecker;
import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Dashboard extends Activity implements GestureOverlayView.OnGesturePerformedListener {
	private static final String TAG = Dashboard.class.getSimpleName();

	public static final int BACKGROUND_CONTROLLER = 2;
	public static final int NOTIFICATION_CONTROLLER = 1;

	private BluetoothAdapter mBluetoothAdapter = null;
	private GestureLibrary gestureLibrary;
	private GestureOverlayView gestureOverlayView;

	// plugins
	private LastFM lastFm;

	//the first part of this string have to be the package name
	private static final String ACTION_USB_PERMISSION = "ca.efriesen.lydia.action.USB_PERMISSION";
	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;
	private UsbAccessory mUsbAccessory;

	private BackgroundController backgroundController;
	private NotificationController notificationController;

	/**
	 * Called when the activities is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);

		backgroundController = new BackgroundController(this);
		notificationController = new NotificationController(this);

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

		gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
		gestureLibrary.load();

		gestureOverlayView = new GestureOverlayView(this);
		View inflate = getLayoutInflater().inflate(R.layout.dashboard, null);

		gestureOverlayView.addView(inflate);
		gestureOverlayView.addOnGesturePerformedListener(this);
		gestureOverlayView.setGestureColor(Color.TRANSPARENT);
		gestureOverlayView.setUncertainGestureColor(Color.TRANSPARENT);

		setContentView(gestureOverlayView);

		// start the hardware manager service
		startService(new Intent(this, HardwareManagerService.class));

		// start the media service
		startService(new Intent(this, MediaService.class));

		getFragmentManager().beginTransaction()
			.replace(R.id.header_fragment, new HeaderFragment())
			.replace(R.id.driver_controls, new DriverControlsFragment())
			.replace(R.id.home_screen_fragment, new HomeScreenFragment())
			.replace(R.id.passenger_controls, new PassengerControlsFragment())
			.replace(R.id.footer_fragment, new FooterFragment())
			.commit();

		// start with the default music notification
		notificationController.addNotification(MusicNotificationFragment.class, NotificationInterface.PRIORITY_HIGH);
		notificationController.addNotification(SystemNotificationFragment.class, NotificationInterface.PRIORITY_LOW);

		// start with the music
		notificationController.setNotification(MusicNotificationFragment.class);

		// initialize all plugins
		lastFm = new LastFM(this);

		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);

		// ensure our admin buttons are up to date
		ButtonConfigDataSource dataSource = new ButtonConfigDataSource(this);
		dataSource.open();
		dataSource.checkRequiredButtons(this);
		dataSource.close();
	}

	@Override
	public void onResume() {
		super.onResume();
		notificationController.onResume();

		checkGooglePlayServices();

		// only add the listener for > kitkat
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
				@Override
				public void onSystemUiVisibilityChange(int visibility) {
					// android studio complains if this isn't here...
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						if(visibility == 0) {
							getWindow().getDecorView().setSystemUiVisibility(
									View.SYSTEM_UI_FLAG_LAYOUT_STABLE
											| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
											| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
											| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
											| View.SYSTEM_UI_FLAG_FULLSCREEN
											| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
							);
						}
					}
				}
			});
		}

		backgroundController.applyBackground();

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
		notificationController.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
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

	@Override
	public void onBackPressed() {
		// get the generic home screen fragment from the tag
		Fragment homeScreeFragment = getFragmentManager().findFragmentById(R.id.home_screen_fragment);

		// if we're home, do nothing
		if (homeScreeFragment instanceof HomeScreenFragment) {
			return;
		}

		// if the fragment isn't null, and implements the onbackpressed callback, do it.
		if (homeScreeFragment != null) {
			if (homeScreeFragment instanceof FragmentOnBackPressedCallback) {
				((FragmentOnBackPressedCallback)homeScreeFragment).onBackPressed();
				return;
			}
		}
		// otherwise just pop the back stack
		super.onBackPressed();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE
								| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
								| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_FULLSCREEN
								| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				);
			}
		}
	}

/* ------------------ END OVERRIDES ------------------ */

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

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);
		for (Prediction prediction : predictions) {
			if (prediction.score > 1.0) {
				LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

				if (prediction.name.equalsIgnoreCase("right")) {
					localBroadcastManager.sendBroadcast(new Intent(MediaService.MEDIA_COMMAND).putExtra(MediaService.MEDIA_COMMAND, MediaService.NEXT));
					// show the music bar on change
					((NotificationController) getController(NOTIFICATION_CONTROLLER)).setNotification(MusicNotificationFragment.class);
				} else {
					localBroadcastManager.sendBroadcast(new Intent(MediaService.MEDIA_COMMAND).putExtra(MediaService.MEDIA_COMMAND, MediaService.PREVIOUS));
					((NotificationController) getController(NOTIFICATION_CONTROLLER)).setNotification(MusicNotificationFragment.class);
				}
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

	public Controller getController(int controller) {
		switch (controller) {
			case NOTIFICATION_CONTROLLER:
				return notificationController;
			case BACKGROUND_CONTROLLER:
				return backgroundController;
		}
		return null;
	}

	public GestureOverlayView getGestureOverlayView() { return gestureOverlayView;}

}