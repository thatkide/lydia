package ca.efriesen.lydia.devices.interfaces;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

/**
 * Created by eric on 2014-08-23.
 */
public class ArduinoHost extends ArduinoInterface {

	private static final String TAG = ArduinoHost.class.getSimpleName();

	private UsbSerialPort port;

	public ArduinoHost(Context context) {
		super(context);
	}

	@Override
	public void onStart(Intent intent) {
		Log.d(TAG, "got a device");
		UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
		if (availableDrivers.isEmpty()) {
			return;
		}

		UsbSerialDriver driver = availableDrivers.get(0);
		UsbDeviceConnection connection = usbManager.openDevice(driver.getDevice());

		if (connection == null) {
			return;
		}

		port = driver.getPorts().get(0);
		try {
			port.open(connection);
			port.setParameters(115200, 8, 1, 0);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {

	}

	@Override
	public int read(byte[] buffer) {
		try {
			port.write(buffer, 500);
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
