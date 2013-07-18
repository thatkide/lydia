package ca.efriesen.lydia_common.includes;

/**
 * User: eric
 * Date: 2013-07-11
 * Time: 11:13 PM
 */
public class Intents {
	// intents used in serial comms with arduino
	public static final String DEFROSTER = "ca.efriesen.lydia.DEFROSTER";
	public static final String INSIDETEMPERATURE = "ca.efriesen.lydia.INSIDETEMPERATURE";
	public static final String GETTEMPERATURE = "ca.efriesen.lydia.GETTEMPERATURE";
	public static final String LIGHTVALUE = "ca.efriesen.lydia.LIGHTVALUE";
	public static final String LOCKSCREEN = "ca.efriesen.lydia.LOCKSCREEN";
	public static final String OUTISETEMPERATURE = "ca.efriesen.lydia.OUTSIDETEMPERATURE";
	public static final String SEATHEAT = "ca.efriesen.lydia.SEATHEAT";
	public static final String UNLOCKSCREEN = "ca.efriesen.lydia.UNLOCKSCREEN";
	public static final String UPDATEBRIGHTNESS = "ca.efriesen.lydia.UPDATEBRIGHTNESS";
	public static final String WINDOWCONTROL = "ca.efriesen.lydia.WINDOW";
	public static final String WIPE = "ca.efriesne.lydia.WIPE";
	public static final String WIPERS = "ca.efriesen.lydia.WIPERS";
	public static final String WIPERDELAY = "ca.efriesen.lydia.WIPERDELAY";

	// intents for the media
	public static final String GETPOSITION = "ca.efriesen.lydia.getPosition";
	public static final String MEDIASTATE = "ca.efriesen.lydia.mediaState";
	public static final String NEXT = "ca.efriesen.lydia.next";
	public static final String PLAYLIST = "ca.efriesen.lydia.playlist";
	public static final String PLAYPAUSE = "ca.efriesen.lydia.playpause";
	public static final String PREVIOUS = "ca.efriesen.lydia.previous";
	public static final String REPEAT = "ca.efriesen.lydia.repeat";
	public static final String SETPOSITION = "ca.efriesen.lydia.setPosition";
	public static final String SHUFFLE = "ca.efriesen.lydia.shuffle";
	public static final String SHUFFLEALL = "ca.efriesen.lydia.SHUFFLEALL";
	public static final String SONGFINISHED = "ca.efriesen.lydia.songFinished";
	public static final String POSITION = "ca.efriesen.lydia.POSITION";
	public static final String UPDATEMEDIAINFO = "ca.efriesen.lydia.UPDATEINFO";

	// intents for bluetooth comms
	public static final String BLUETOOTHMANAGER = "ca.efriesen.lydia.BluetoothManager";
	public static final String BLUEOOTHCONNECTED = "ca.efriesen.lydia.BluetoothConnected";
	public static final String BLUEOOTHDISCONNECTED = "ca.efriesen.lydia.BluetoothDisconnected";
	public static final String BLUETOOTHTOGGLE = "ca.efriesen.lydia.BluetoothToggle";
	public static final String INCOMINGCALL = "ca.efriesen.lydia.IncomingCall";
	public static final String SMSRECEIVED = "ca.efriesen.lydia.SMSReceived";
	public static final String SMSREPLY = "ca.efriesen.lydia.SMSReply";

	// intents for other process stuff
	public static final String DRAWMARKER = "ca.efriesen.lydia.DrawMarker";
	public static final String GETDIRECTIONS = "ca.efriesen.lydia.GetDirections";
}
