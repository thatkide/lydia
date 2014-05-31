package ca.efriesen.lydia_common.includes;

import android.graphics.Color;

/**
 * User: eric
 * Date: 2013-07-11
 * Time: 11:14 PM
 */
public class Constants {

	final public static int FilterColor = Color.rgb(30, 140, 180);

	// I2C address of slave devices
	final public static int ALARM = 14;

	// commands for slaves
	final public static int AUTOARM = 0x32;
	final public static int AUTOARMDELAY = 0x33;
	final public static int EEPROM = 0x31;

	// These are for serial commands sent to the arduino
	// 160 - 176
	final public static byte DRIVERSEAT = (byte)171;
	final public static byte DWINDOWUP = (byte)163;
	final public static byte DWINDOWDOWN = (byte)164;
	final public static byte DWINDOWSTOP = (byte)165;
	final public static byte GETTEMPERATURE = (byte)162;
	final public static byte GETLIGHT = (byte)175;
	final public static byte LIGHTSENSOR = (byte)176;
	final public static byte INSIDETEMPERATURESENSOR = (byte)160; // 0xA0
	final public static byte OUTSIDETEMPERATURESENSOR = (byte)161; // 0xA1
	final public static byte PASSENGERSEAT = (byte)172;
	final public static byte PWINDOWUP = (byte)166;
	final public static byte PWINDOWDOWN = (byte)167;
	final public static byte PWINDOWSTOP = (byte)168;
	final public static byte REARWINDOWDEFROSTER = (byte)173;
	final public static byte SCREENLOCK = (byte)169;
	final public static byte SCREENUNLOCK = (byte)170;
	final public static byte WIPE = (byte)174;

	// MJLJ commands
	final public static int MJLJ = 182;
	final public static int GETADVANCE = 177;
	final public static int GETFLAGS = 181;
	final public static int GETLOAD = 178;
	final public static int GETRPM = 179;
	final public static int GETRUNNING = 180;


	// we need an id, but this isn't used for hardware io comms
	final public static int WINDOWS = 100;
	final public static int PINGREQUEST = 0x09;
	final public static int PINGREPLY = 0x10;
	final public static int RFIDNUMBER = 0x30;

	// Preferences contstants
	final public static String REPEATALL = "repeatAll";
	final public static String SHUFFLE = "shuffleMusic";
}
