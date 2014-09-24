package com.autosenseapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import com.autosenseapp.activities.Dashboard;
import com.autosenseapp.activities.settings.ArduinoPinEditor;
import com.autosenseapp.activities.settings.ButtonEditor;
import com.autosenseapp.buttons.BaseButton;
import com.autosenseapp.buttons.appButtons.AppLaunchButton;
import com.autosenseapp.buttons.widgetButtons.ArduinoButton;
import com.autosenseapp.controllers.BackgroundController;
import com.autosenseapp.devices.configs.ArduinoDue;
import com.autosenseapp.devices.configs.ArduinoUno;
import com.autosenseapp.fragments.HeaderFragment;
import com.autosenseapp.fragments.Settings.BackgroundSettingsFragment;
import com.autosenseapp.fragments.Settings.MasterIoFragment;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;

/**
 * Created by eric on 2014-09-13.
 */
@Module(
		injects = {
				AppLaunchButton.class,
				ArduinoButton.class,
				ArduinoDue.class,
				ArduinoUno.class,
				ArduinoPinEditor.class,
				BackgroundController.class,
				BackgroundSettingsFragment.class,
				BaseButton.class,
				ButtonEditor.class,
				Dashboard.class,
				HeaderFragment.class,
				MasterIoFragment.class,
		},
		library = true,
		complete =  false
)
public class AndroidModule {

	private final AutosenseApplication application;

	public AndroidModule(@ForApplication AutosenseApplication application) {
		this.application = application;
	}

	@Provides @Singleton
	AudioManager proivideAudioManager() {
		// setup the audio manager from the main activity
		return (AudioManager) application.getSystemService(Context.AUDIO_SERVICE);
	}

	@Provides @Singleton
	Context provideApplicationContext() {
		return application;
	}

	@Provides @Singleton
	LayoutInflater provideLayoutInflater() {
		return (LayoutInflater) application.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Provides @Singleton
	LocalBroadcastManager provideLocalBroadcastManager() {
		return LocalBroadcastManager.getInstance(application);
	}

	@Provides @Singleton
	LocationManager provideLocationManager() {
		return (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
	}

	@Provides @Singleton
	Resources provideResources() {
		return application.getResources();
	}

	@Provides @Singleton
	SharedPreferences provideSharedPreferences() {
		return application.getSharedPreferences(application.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
	}

	@Provides @Singleton
	UsbManager provideUsbManager() {
		return (UsbManager) application.getSystemService(Context.USB_SERVICE);
	}
}
