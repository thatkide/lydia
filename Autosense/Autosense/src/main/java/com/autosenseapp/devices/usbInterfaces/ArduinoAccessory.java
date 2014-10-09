package com.autosenseapp.devices.usbInterfaces;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.autosenseapp.AutosenseApplication;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.inject.Inject;

/**
 * Created by eric on 2014-08-26.
 */
public class ArduinoAccessory implements ArduinoInterface {

	private static final String TAG = ArduinoAccessory.class.getSimpleName();

	@Inject UsbManager usbManager;
	private ParcelFileDescriptor mFileDescriptor;
	private InputStream mInputStream;
	private OutputStream mOutputStream;

	@Override
	public void onCreate(Context context, Intent intent) {
		// Inject our self to the DI graph
		((AutosenseApplication)context.getApplicationContext()).inject(this);
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
				}
			}
		}
	}

	@Override
	public void onDestroy() {
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
		}
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		return mInputStream.read(buffer);
	}

	@Override
	public void write(byte[] data) {
		try {
			mOutputStream.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
}
