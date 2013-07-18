package ca.efriesen.lydia_common.includes;

/**
 * User: eric
 * Date: 2013-07-11
 * Time: 11:14 PM
 */
public class Constants {

	// These are for serial commands sent to the arduino
	// 160 - 176
	final public static int DRIVERSEAT = 171;
	final public static int DWINDOWUP = 163;
	final public static int DWINDOWDOWN = 164;
	final public static int DWINDOWSTOP = 165;
	final public static int GETTEMPERATURE = 162;
	final public static int GETLIGHT = 175;
	final public static int LIGHTSENSOR = 176;
	final public static int INSIDETEMPERATURESENSOR = 160; // 0xA0
	final public static int OUTSIDETEMPERATURESENSOR = 161; // 0xA1
	final public static int PASSENGERSEAT = 172;
	final public static int PWINDOWUP = 166;
	final public static int PWINDOWDOWN = 167;
	final public static int PWINDOWSTOP = 168;
	final public static int REARWINDOWDEFROSTER = 173;
	final public static int SCREENLOCK = 169;
	final public static int SCREENUNLOCK = 170;
	final public static int WIPE = 174;

	final public static int FLPRESSURESENSOR = 177;


	// we need an id, but this isn't used for hardware io comms
	final public static int WINDOWS = 100;

	// Preferences contstants
	final public static String REPEATALL = "repeatAll";
	final public static String SHUFFLE = "shuffleMusic";
	public static final String PhoneAddress = "phone_address";


	// Lastfm stuff
	final public static String lastFmKey = "a750c79a8a305077843fe3023554b5ac";
	final public static String lastFmSecret = "883a295bec7c9e532d2da8ebde7b45d8";

}
