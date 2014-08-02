package ca.efriesen.lydia.includes;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.FileNotFoundException;

/**
 * Created by eric on 2014-08-01.
 */
public class ImageHelper {

	public static Bitmap decodeUri(Activity activity, Uri selectedImage) {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;

		try {
			BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(selectedImage), null, o);
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
			return BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(selectedImage), null, o2);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
