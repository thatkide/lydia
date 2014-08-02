package ca.efriesen.lydia.fragments.Settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia.includes.ImageHelper;

/**
 * Created by eric on 2014-08-01.
 */
public class BackgroundSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

	private static final String TAG = BackgroundSettingsFragment.class.getSimpleName();

	public static final String USE_BG_IMAGE = "use_bg_image";
	public static final String BG_IMG_PATH = "bg_img_path";

	private Activity activity;
	private SharedPreferences sharedPreferences;
	private RelativeLayout layout;

	private static final int ACTIVITY_SELECT_IMAGE = 100;

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

		findPreference("imageChooser").setOnPreferenceClickListener(this);
		findPreference("removeImage").setOnPreferenceClickListener(this);

		layout = (RelativeLayout) activity.findViewById(R.id.dashboard_container);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equalsIgnoreCase("imageChooser")) {
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			startActivityForResult(photoPickerIntent, ACTIVITY_SELECT_IMAGE);
		} else if (preference.getKey().equalsIgnoreCase("removeImage")) {
			sharedPreferences.edit().putBoolean(USE_BG_IMAGE, false).putString(BG_IMG_PATH, "").apply();
			layout.setBackground(null);
		}
		return true;
	}



	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent imageReturned) {
		super.onActivityResult(requestCode, resultCode, imageReturned);

		switch (requestCode) {
			case ACTIVITY_SELECT_IMAGE: {
				if (resultCode == Activity.RESULT_OK) {

					final Bitmap bitmap = ImageHelper.decodeUri(activity, imageReturned.getData());
					layout.setBackground(new BitmapDrawable(getResources(), bitmap));
					RelativeLayout colorMask = (RelativeLayout) activity.findViewById(R.id.color_mask);
					colorMask.setBackgroundColor(Color.argb(0xAA, 0x22, 0x23, 0x22));

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
						}
					});

					builder.create().show();
				}
			}
		}
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


