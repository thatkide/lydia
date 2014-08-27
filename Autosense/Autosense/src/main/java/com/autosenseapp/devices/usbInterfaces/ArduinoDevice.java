package com.autosenseapp.devices.usbInterfaces;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

/**
 * Created by eric on 2014-08-26.
 */
public class ArduinoDevice implements ArduinoInterface {

	private static final String TAG = ArduinoDevice.class.getSimpleName();

	private UsbSerialPort port;

	@Override
	public void onCreate(Context context, Intent intent) {
		UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

		List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);

		UsbSerialDriver driver = drivers.get(0);

		if (driver.getDevice().getDeviceId() == usbDevice.getDeviceId()) {
			UsbDeviceConnection connection = usbManager.openDevice(usbDevice);

			if (connection == null) {
				// error
				Log.e(TAG, "no connection");
			}

			port = driver.getPorts().get(0);
			try {
				port.open(connection);
				port.setParameters(115200, 8, 1, 0);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroy() {
		try {
			port.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int read(byte[] buffer) {
		try {
			return port.read(buffer, 500);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void write(byte[] data) {
		try {
			port.write(data, 500);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
