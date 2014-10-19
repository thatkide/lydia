package com.autosenseapp.activities;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Color;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import com.autosenseapp.BuildConfig;
import com.autosenseapp.R;
import com.autosenseapp.callbacks.FragmentOnBackPressedCallback;
import com.autosenseapp.controllers.ArduinoController;
import com.autosenseapp.controllers.BackgroundController;
import com.autosenseapp.controllers.MediaController;
import com.autosenseapp.controllers.NotificationController;
import com.autosenseapp.databases.ButtonConfigDataSource;
import com.autosenseapp.fragments.DriverControlsFragment;
import com.autosenseapp.fragments.FooterFragment;
import com.autosenseapp.fragments.HeaderFragment;
import com.autosenseapp.fragments.HomeScreenFragment;
import com.autosenseapp.fragments.NotificationFragments.MusicNotificationFragment;
import com.autosenseapp.fragments.NotificationFragments.SystemNotificationFragment;
import com.autosenseapp.fragments.PassengerControlsFragment;
import com.autosenseapp.includes.Helpers;
import com.autosenseapp.interfaces.NotificationInterface;
import com.autosenseapp.plugins.LastFM;
import com.appaholics.updatechecker.UpdateChecker;
import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import javax.inject.Inject;

public class Dashboard extends BaseActivity implements GestureOverlayView.OnGesturePerformedListener {
	private static final String TAG = Dashboard.class.getSimpleName();

	private BluetoothAdapter mBluetoothAdapter = null;
	private GestureLibrary gestureLibrary;
	private GestureOverlayView gestureOverlayView;

	@Inject	ArduinoController arduinoController;
	@Inject BackgroundController backgroundController;
	@Inject MediaController mediaController;
	@Inject NotificationController notificationController;
	@Inject SharedPreferences sharedPreferences;

	// plugins
	private LastFM lastFm;

	//the first part of this string have to be the package name
	private static final String ACTION_USB_PERMISSION = "com.autosenseapp.action.USB_PERMISSION";
	private PendingIntent usbPermissionIntent;
	private boolean mPermissionRequestPending;

	@Inject UsbManager usbManager;

	/**
	 * Called when the activities is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);

		notificationController.onCreate(this);

		setupUpdateChecker();

		if (BuildConfig.INCLUDE_BUGSENSE) {
			BugSenseHandler.initAndStartSession(Dashboard.this, getString(R.string.bugsenseApiKey));
		}

		setupGestures();
		setContentView(gestureOverlayView);

		getFragmentManager().beginTransaction()
			.replace(R.id.header_fragment, new HeaderFragment())
			.replace(R.id.driver_controls, new DriverControlsFragment())
			.replace(R.id.home_screen_fragment, new HomeScreenFragment())
			.replace(R.id.passenger_controls, new PassengerControlsFragment())
			.replace(R.id.footer_fragment, new FooterFragment())
			.commit();

		setupNotifications();

		lastFm = new LastFM(this);

		usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter intentFilter = new IntentFilter(ACTION_USB_PERMISSION);
		intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(usbActionReceiver, intentFilter);

		checkHomescreenButtons();
	}

	@Override
	public void onResume() {
		super.onResume();
		notificationController.onResume();

		checkGooglePlayServices();

		Helpers.setupFullScreen(getWindow());

		backgroundController.applyBackground(this);

		setupArduino();

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

	}

	@Override
	public void onPause() {
		super.onPause();
		notificationController.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		try {
			lastFm.destroy();
		} catch (Exception e) { }
		try {
			unregisterReceiver(usbActionReceiver);
		} catch (Exception e) {}
	}

	@Override
	public void onBackPressed() {
		Fragment homeScreeFragment = getFragmentManager().findFragmentById(R.id.home_screen_fragment);

		// if we're home, do nothing
		if (homeScreeFragment instanceof HomeScreenFragment) {
			return;
		}

		try {
			if (homeScreeFragment instanceof FragmentOnBackPressedCallback) {
				((FragmentOnBackPressedCallback)homeScreeFragment).onBackPressed();
			}
		} catch (NullPointerException e) {
			super.onBackPressed();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			Helpers.setupFullScreen(getWindow());
		}
	}

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

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);
		for (Prediction prediction : predictions) {
			if (prediction.score > 1.0) {
				if (prediction.name.equalsIgnoreCase("right")) {
					mediaController.next();
					// show the music bar on change
					notificationController.setNotification(MusicNotificationFragment.class);
				} else {
					mediaController.previous();
					notificationController.setNotification(MusicNotificationFragment.class);
				}
			}
		}
	}

	// Receiver for the USB intents
	// this is fired on attach and permission granted
	private final BroadcastReceiver usbActionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// check if permission intent
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					// if permission was granted
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						// start the service and pass the intent
						Intent serviceIntent = new Intent();
						serviceIntent.putExtras(intent);
						arduinoController.onStart(serviceIntent);
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
			}
		}
	};

	private void setupArduino() {
		if (!arduinoController.isAccessoryRunning()) {
			sharedPreferences.edit().putInt(ArduinoController.ARDUINO_TYPE, ArduinoController.ARDUINO_NONE).apply();
			// Try to connect a USB accessory first
			UsbAccessory[] accessories = usbManager.getAccessoryList();
			// get first accessory
			UsbAccessory accessory = (accessories == null ? null : accessories[0]);

			if (accessory != null) {
				if (usbManager.hasPermission(accessory)) {
					Intent arduinoIntent = new Intent();
					arduinoIntent.putExtra(UsbManager.EXTRA_ACCESSORY, accessory);
					arduinoController.onStart(arduinoIntent);
				} else {
					synchronized (usbActionReceiver) {
						if (mPermissionRequestPending) {
							usbManager.requestPermission(accessory, usbPermissionIntent);
							mPermissionRequestPending = true;
						}
					}
				}
			} else {
				HashMap<String, UsbDevice> devices = usbManager.getDeviceList();

				for (String deviceName : devices.keySet()) {
					UsbDevice device = devices.get(deviceName);

					Intent intent = new Intent();
					intent.putExtra(UsbManager.EXTRA_DEVICE, device);
					arduinoController.onStart(intent);
				}
			}
		}
	}

	public GestureOverlayView getGestureOverlayView() { return gestureOverlayView;}

	private void checkHomescreenButtons() {
		// ensure our admin buttons are up to date
		ButtonConfigDataSource dataSource = new ButtonConfigDataSource(this);
		dataSource.open();
		dataSource.checkRequiredButtons(this);
		dataSource.close();
	}

	private void setupGestures() {
		gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
		gestureLibrary.load();

		gestureOverlayView = new GestureOverlayView(this);
		View inflate = getLayoutInflater().inflate(R.layout.dashboard, null);

		gestureOverlayView.addView(inflate);
		gestureOverlayView.addOnGesturePerformedListener(this);
		gestureOverlayView.setGestureColor(Color.TRANSPARENT);
		gestureOverlayView.setUncertainGestureColor(Color.TRANSPARENT);
	}

	private void setupNotifications() {
		notificationController.addNotification(MusicNotificationFragment.class, NotificationInterface.PRIORITY_HIGH);
		notificationController.addNotification(SystemNotificationFragment.class, NotificationInterface.PRIORITY_LOW);

		// start with the music
		notificationController.setNotification(MusicNotificationFragment.class);
	}

	private void setupUpdateChecker() {
		if (BuildConfig.INCLUDE_UPDATER) {
			final UpdateChecker checker = new UpdateChecker(this, true);
			checker.addObserver(new Observer() {
				@Override
				public void update(Observable observable, Object o) {
					if (checker.isUpdateAvailable()) {
						checker.downloadAndInstall(getString(R.string.update_apk_url));
					}
				}
			});

			checker.checkForUpdateByVersionCode(getString(R.string.update_url));
		}
	}
}