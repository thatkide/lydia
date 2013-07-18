package ca.efriesen.lydia.interfaces;

import com.hoho.android.usbserial.util.SerialInputOutputManager;

/**
 * Created by eric on 2013-05-28.
 */
public interface HardwareIO {

	public void setIOManager(Object ioManager);

	public void write(byte[] command);
}
