package ca.efriesen.lydia.devices.interfaces;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by eric on 2014-08-23.
 */
public class ArduinoAccessory extends ArduinoInterface {

	private static final String TAG = ArduinoAccessory.class.getSimpleName();

	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private ParcelFileDescriptor mFileDescriptor;

	private UsbAccessory mAccessory = null;

	public ArduinoAccessory(Context context) {
		super(context);
	}

	@Override
	public void onStart(Intent intent) {
		if (mAccessory == null) {
			UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
			UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);

			if (accessory != null) {
				// FIXME
				// I can't get the USB_ACCESSORY_DETACHED event to fire.  So this will close the accessory before opening every time
				//closeAccessory();
				if (usbManager.hasPermission(accessory)) {
					mFileDescriptor = usbManager.openAccessory(accessory);
					if (mFileDescriptor != null) {
						FileDescriptor fd = mFileDescriptor.getFileDescriptor();
						mInputStream = new FileInputStream(fd);
						mOutputStream = new FileOutputStream(fd);
						mAccessory = accessory;

					} else {
						Log.d(TAG, "accessory open failed");
					}
				} else {
					Log.d(TAG, "accessory permission denied");
				}
			}
		}
	}

	@Override
	public void close() {
		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
			if (mInputStream != null) {
				mInputStream.close();
			}
			if (mOutputStream != null) {
				mOutputStream.close();
			}
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		} finally {
			mFileDescriptor = null;
			mInputStream = null;
			mOutputStream = null;
			mAccessory = null;
		}
	}

	@Override
	public int read(byte[] buffer) {
		try {
			return mInputStream.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void write(byte[] data) {
		try {
			mOutputStream.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
