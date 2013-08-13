package ca.efriesen.lydia.interfaces;

/**
 * Created by eric on 2013-05-28.
 */
public interface HardwareIO {

	public void setIOManager(Object ioManager);

	public void write(byte[] command);
}
