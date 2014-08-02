package ca.efriesen.lydia.fragments.Settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.RelativeLayout;
import net.jayschwa.android.preference.SliderPreference;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.includes.Helpers;
import ca.efriesen.lydia.includes.ImageHelper;

/**
 * Created by eric on 2014-08-01.
 */
public class BackgroundSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

	private static final String TAG = BackgroundSettingsFragment.class.getSimpleName();

	public static final String USE_BG_IMAGE = "use_bg_image";
	public static final String BG_BRIGHTNESS = "bg_brightness";
	public static final String BG_IMG_PATH = "bg_img_path";

	private Activity activity;
	private SharedPreferences sharedPreferences;
	private RelativeLayout layout;
	private RelativeLayout colorMask;

	private static final int ACTIVITY_SELECT_IMAGE = 100;

	public SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
			// if the backlight pref has changed
			if (s.equalsIgnoreCase("backgroundBrightness")) {
				// convert the float of the slider (0.0 - 1.0) to a range of 0-255
				float brightness = sharedPreferences.getFloat("backgroundBrightness", 0);
				colorMask.setBackgroundColor(Color.argb(Helpers.map(brightness, 0, 1, 255, 0), 0x00, 0x00, 0x00));
				sharedPreferences.edit().putFloat(BG_BRIGHTNESS, brightness).apply();
			} else if (s.equalsIgnoreCase("topBgColor") || s.equalsIgnoreCase("bottomBgColor")) {
				GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {sharedPreferences.getInt("topBgColor", 1), sharedPreferences.getInt("bottomBgColor", 1)});
				gradientDrawable.setCornerRadius(0f);
				layout.setBackground(gradientDrawable);
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

		sharedPreferences = activity.getSharedPreferences(getActivity().getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		sharedPreferences.registerOnSharedPreferenceChangeListener(mListener);

		// auto update the background brightness on slider change
		((SliderPreference) findPreference("backgroundBrightness")).setAutoUpdate(true);

		// set the preferences onclick listener to this class
		findPreference("imageChooser").setOnPreferenceClickListener(this);
		findPreference("removeImage").setOnPreferenceClickListener(this);
		// get the two layouts. dashboard container is the lowest, we set the image there and the mask is to darken the image up
		layout = (RelativeLayout) activity.findViewById(R.id.dashboard_container);
		colorMask = (RelativeLayout) activity.findViewById(R.id.color_mask);
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
			defaultBackground();
		}
		return true;
	}



	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent imageReturned) {
		super.onActivityResult(requestCode, resultCode, imageReturned);

		switch (requestCode) {
			case ACTIVITY_SELECT_IMAGE: {
				if (resultCode == Activity.RESULT_OK) {
					// decode the image passed.  this will resize for us
					final Bitmap bitmap = ImageHelper.decodeUri(activity, imageReturned.getData());
					// set the background
					layout.setBackground(new BitmapDrawable(getResources(), bitmap));
					// set the color mask
					colorMask.setBackgroundColor(Color.argb(0, 0x00, 0x00, 0x00));
					// reset the brightness
					sharedPreferences.edit().putFloat(BG_BRIGHTNESS, 1).apply();

					// create a new alert asking to save or cancel
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
					builder.setTitle(activity.getString(R.string.background_set));
					builder.setPositiveButton(activity.getString(R.string.save), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// save image
							sharedPreferences.edit().putBoolean(USE_BG_IMAGE, true).apply();
							new SaveImage(activity).execute(bitmap);
						}
					})
					.setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// don't save image, revert
							defaultBackground();
						}
					});

					builder.create().show();
				}
			}
		}
	}

	private void defaultBackground() {
		// remove the background image and go back to default
		sharedPreferences.edit().putBoolean(USE_BG_IMAGE, false).putString(BG_IMG_PATH, "").apply();
		sharedPreferences.edit().putInt("topBgColor", 0).putInt("bottomBgColor", 0).apply();
		layout.setBackground(null);
		colorMask.setBackground(null);
	}

	private class SaveImage extends AsyncTask<Bitmap,Void,Void> {

		private Activity activity;

		public SaveImage(Activity activity) {
			this.activity = activity;
		}

		@Override
		protected Void doInBackground(Bitmap... params) {
			Bitmap bitmap = params[0];
			FileOutputStream out = null;
			try {
				ContextWrapper wrapper = new ContextWrapper(activity);
				File directory = wrapper.getDir("images", Context.MODE_MULTI_PROCESS);
				File imagePath = new File(directory, "background");

				out = new FileOutputStream(imagePath);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
				sharedPreferences.edit().putString(BG_IMG_PATH, imagePath.getAbsolutePath()).apply();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}
}


