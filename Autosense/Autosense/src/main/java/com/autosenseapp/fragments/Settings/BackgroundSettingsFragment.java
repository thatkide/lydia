package com.autosenseapp.fragments.Settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.Preference;
import android.widget.Toast;
import com.autosenseapp.controllers.BackgroundController;
import net.jayschwa.android.preference.SliderPreference;
import com.autosenseapp.R;
import com.splunk.mint.Mint;
import javax.inject.Inject;
import yuku.ambilwarna.widget.AmbilWarnaPreference;

/**
 * Created by eric on 2014-08-01.
 */
public class BackgroundSettingsFragment extends BasePreferenceFragment implements Preference.OnPreferenceClickListener {

	private static final String TAG = BackgroundSettingsFragment.class.getSimpleName();

	private Activity activity;

	@Inject BackgroundController backgroundController;

	private static final int ACTIVITY_SELECT_IMAGE = 100;

	public SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
			if (s.equalsIgnoreCase("backgroundBrightness")) {
				// convert the float of the slider (0.0 - 1.0) to a range of 0-255
				float brightness = sharedPreferences.getFloat("backgroundBrightness", 0);
				backgroundController.setBrightness(brightness);
			} else if (s.equalsIgnoreCase("topBgColor") || s.equalsIgnoreCase("bottomBgColor")) {
				int topColor = sharedPreferences.getInt("topBgColor", 1);
				int bottomColor = sharedPreferences.getInt("bottomBgColor", 1);
				// update the onscreen colors
				((AmbilWarnaPreference) findPreference("topBgColor")).forceSetValue(topColor);
				((AmbilWarnaPreference) findPreference("bottomBgColor")).forceSetValue(bottomColor);
				backgroundController.setBackgroundColor(sharedPreferences.getInt("topBgColor", 1), sharedPreferences.getInt("bottomBgColor", 1));
			}
		}
	};

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		addPreferencesFromResource(R.xml.background_preferences_fragment);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		activity = getActivity();

		sharedPreferences.registerOnSharedPreferenceChangeListener(mListener);

		// auto update the background brightness on slider change
		((SliderPreference) findPreference("backgroundBrightness")).setAutoUpdate(true);

		// set the preferences onclick listener to this class
		findPreference("imageChooser").setOnPreferenceClickListener(this);
		findPreference("removeImage").setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		// one of the buttons was pressed
		if (preference.getKey().equalsIgnoreCase("imageChooser")) {
			// if it was the image chooser, launch an intent to get an image
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			startActivityForResult(photoPickerIntent, ACTIVITY_SELECT_IMAGE);
		} else if (preference.getKey().equalsIgnoreCase("removeImage")) {
			// remove the background image and go back to default
			backgroundController.setDefaultBackground();
		}
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent imageReturned) {
		super.onActivityResult(requestCode, resultCode, imageReturned);

		switch (requestCode) {
			case ACTIVITY_SELECT_IMAGE: {
				if (resultCode == Activity.RESULT_OK) {
					try {
						// pass the uri and get the bitmap back
						final Bitmap bitmap = backgroundController.setBackgroundImage(activity, imageReturned.getData());

						// create a new alert asking to save or cancel
						AlertDialog.Builder builder = new AlertDialog.Builder(activity);
						builder.setTitle(activity.getString(R.string.background_set));
						builder.setPositiveButton(activity.getString(R.string.save), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// if the user clicked yes, save the image
								backgroundController.saveImage(activity, bitmap);
							}
						})
								.setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// don't save image, revert
										backgroundController.setDefaultBackground();
									}
								});

						builder.create().show();
					} catch (NullPointerException e) {
						Toast.makeText(getActivity(), "Error, Sending Bug Report", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
						Mint.logException(e);
					}
				}
			}
		}
	}


}


