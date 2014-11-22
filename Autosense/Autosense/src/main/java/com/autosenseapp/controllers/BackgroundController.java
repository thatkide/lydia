package com.autosenseapp.controllers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.RelativeLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import com.autosenseapp.R;
import com.autosenseapp.includes.Helpers;
import javax.inject.Inject;
import javax.inject.Singleton;
import ca.efriesen.lydia_common.media.Song;

/**
 * Created by eric on 2014-08-05.
 */
@Singleton
public class BackgroundController {

	public static final String USE_BG_IMAGE = "use_bg_image";
	public static final String BG_BRIGHTNESS = "bg_brightness";
	public static final String BG_IMG_PATH = "bg_img_path";

	private RelativeLayout layout;
	private RelativeLayout colorMask;

	private LocalBroadcastManager localBroadcastManager;
	@Inject SharedPreferences sharedPreferences;

	@Inject
	public BackgroundController(LocalBroadcastManager localBroadcastManager) {
		this.localBroadcastManager = localBroadcastManager;
		localBroadcastManager.registerReceiver(mediaInfoReceiver, new IntentFilter(MediaController.MEDIA_INFO));
	}

	public void onDestroy() {
		localBroadcastManager.unregisterReceiver(mediaInfoReceiver);
	}

	public void applyBackground(Activity activity) {
		setLayouts(activity);
		// set background image if we have one set
		boolean useBgImg = sharedPreferences.getBoolean(USE_BG_IMAGE, false);
		// if we have opted to use a background image, set it
		if (useBgImg) {
			String imagePath = sharedPreferences.getString(BG_IMG_PATH, "");
			layout.setBackground(new BitmapDrawable(activity.getResources(), BitmapFactory.decodeFile(imagePath)));
			// else if we have a non default top or bottom color, use those
		} else if (sharedPreferences.getInt("topBgColor", 0) != 0 || sharedPreferences.getInt("bottomBgColor", 0) != 0) {
			GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{sharedPreferences.getInt("topBgColor", 1), sharedPreferences.getInt("bottomBgColor", 1)});
			gradientDrawable.setCornerRadius(0f);
			RelativeLayout layout = (RelativeLayout) activity.findViewById(R.id.dashboard_container);
			layout.setBackground(gradientDrawable);
		}
		// set overall brightness
		float brightness = sharedPreferences.getFloat(BG_BRIGHTNESS, 1.0f);
		RelativeLayout colorMask = (RelativeLayout) activity.findViewById(R.id.color_mask);
		colorMask.setBackgroundColor(Color.argb(Helpers.map(brightness, 0, 1, 255, 0), 0x00, 0x00, 0x00));
	}

	public void saveImage(Context context, Bitmap bitmap) {
		// save image
		sharedPreferences.edit().putBoolean(USE_BG_IMAGE, true).apply();
		new SaveImage(context).execute(bitmap);
	}

	public void setBackgroundColor(int topColor, int bottomColor) {
//		setLayouts();
		GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {topColor, bottomColor});
		gradientDrawable.setCornerRadius(0f);
		layout.setBackground(gradientDrawable);
	}

	public void setBackgroundImage(Context context, Bitmap image) {
//		setLayouts();
		// set the background
		layout.setBackground(new BitmapDrawable(context.getResources(), image));
		// set the color mask
		colorMask.setBackgroundColor(Color.argb(0, 0x00, 0x00, 0x00));
		// reset the brightness
		sharedPreferences.edit().putFloat(BG_BRIGHTNESS, 1).apply();
	}

	public Bitmap setBackgroundImage(Context context, Uri uri) {
		// decode the image passed.  this will resize for us
		Bitmap bitmap = decodeUri(context, uri);
		setBackgroundImage(context, bitmap);
		return bitmap;
	}

	// range of 0.0 - 1.0
	public void setBrightness(float brightness) {
//		setLayouts();
		if (brightness > 1) brightness = 1;
		if (brightness < 0) brightness = 0;

		colorMask.setBackgroundColor(Color.argb(Helpers.map(brightness, 0, 1, 255, 0), 0x00, 0x00, 0x00));
		sharedPreferences.edit().putFloat(BG_BRIGHTNESS, brightness).apply();
	}

	public void setDefaultBackground() {
		// remove the background image and go back to default
		sharedPreferences.edit().putBoolean(USE_BG_IMAGE, false).putString(BG_IMG_PATH, "").apply();
		sharedPreferences.edit().putInt("topBgColor", 0).putInt("bottomBgColor", 0).apply();
		layout.setBackground(null);
		colorMask.setBackground(null);
	}

	private static Bitmap decodeUri(Context context, Uri selectedImage) {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;

		try {
			BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// The new size we want to scale to
		final int REQUIRED_SIZE = 512;

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		try {
			return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o2);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void setLayouts(Activity activity) {
		if (layout == null) {
			layout = (RelativeLayout) activity.findViewById(R.id.dashboard_container);
		}
		if (colorMask == null) {
			colorMask = (RelativeLayout) activity.findViewById(R.id.color_mask);
		}
	}

	private class SaveImage extends AsyncTask<Bitmap,Void,Void> {

		private Context context;

		public SaveImage(Context context) {
			this.context = context;
		}

		@Override
		protected Void doInBackground(Bitmap... params) {
			Bitmap bitmap = params[0];
			FileOutputStream out = null;
			try {
				ContextWrapper wrapper = new ContextWrapper(context);
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

	private BroadcastReceiver mediaInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// if we want to use the album art as the background
			if (sharedPreferences.getBoolean("useAlbumArtBg", false)) {
				// get the bitmap from the song
				Bitmap bitmap = (((Song) intent.getSerializableExtra(MediaController.SONG)).getAlbum()).getAlbumArt(context);
				// if it's valid
				if (bitmap != null) {
					// set the background
					setBackgroundImage(context, bitmap);
				}
			}
		}
	};
}
