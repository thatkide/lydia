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
import com.autosenseapp.adapters.MediaAdapter;
import com.autosenseapp.adapters.SongAdapter;
import com.autosenseapp.buttons.BaseButton;
import com.autosenseapp.buttons.appButtons.AppLaunchButton;
import com.autosenseapp.buttons.appButtons.MusicButton;
import com.autosenseapp.buttons.widgetButtons.ArduinoButton;
import com.autosenseapp.controllers.BackgroundController;
import com.autosenseapp.controllers.MediaController;
import com.autosenseapp.devices.actions.ActionToggle;
import com.autosenseapp.devices.configs.ArduinoDue;
import com.autosenseapp.devices.configs.ArduinoUno;
import com.autosenseapp.devices.usbInterfaces.ArduinoAccessory;
import com.autosenseapp.devices.usbInterfaces.ArduinoDevice;
import com.autosenseapp.dialogs.ActionToggleExtraDialog;
import com.autosenseapp.fragments.HeaderFragment;
import com.autosenseapp.fragments.MusicFragment;
import com.autosenseapp.fragments.MusicFragmentStates.AlbumSongState;
import com.autosenseapp.fragments.MusicFragmentStates.AlbumState;
import com.autosenseapp.fragments.MusicFragmentStates.AllSongsState;
import com.autosenseapp.fragments.MusicFragmentStates.ArtistState;
import com.autosenseapp.fragments.MusicFragmentStates.NowPlayingState;
import com.autosenseapp.fragments.MusicFragmentStates.PlaylistSongState;
import com.autosenseapp.fragments.MusicFragmentStates.PlaylistState;
import com.autosenseapp.fragments.NotificationFragments.MusicNotificationFragment;
import com.autosenseapp.fragments.Settings.ArduinoSettingsFragment;
import com.autosenseapp.fragments.Settings.BackgroundSettingsFragment;
import com.autosenseapp.fragments.Settings.MasterIoFragment;
import com.autosenseapp.services.AutosenseService;
import com.autosenseapp.services.media_states.PausedState;
import com.autosenseapp.services.media_states.PlayState;
import com.autosenseapp.services.media_states.StoppedState;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;

/**
 * Created by eric on 2014-09-13.
 */
@Module(
		injects = {
				ActionToggle.class,
				ActionToggleExtraDialog.class,
				AlbumSongState.class,
				AlbumState.class,
				AllSongsState.class,
				AppLaunchButton.class,
				ArduinoAccessory.class,
				ArduinoButton.class,
				ArduinoDevice.class,
				ArduinoDue.class,
				ArduinoUno.class,
				ArduinoSettingsFragment.class,
				ArduinoPinEditor.class,
				ArtistState.class,
				AutosenseService.class,
				BackgroundController.class,
				BackgroundSettingsFragment.class,
				BaseButton.class,
				ButtonEditor.class,
				Dashboard.class,
				HeaderFragment.class,
				MasterIoFragment.class,
				MusicButton.class,
				MediaAdapter.class,
				MediaController.class,
				MusicFragment.class,
				MusicNotificationFragment.class,
				NowPlayingState.class,
				PausedState.class,
				PlayState.class,
				PlaylistState.class,
				PlaylistSongState.class,
				SongAdapter.class,
				StoppedState.class,
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
