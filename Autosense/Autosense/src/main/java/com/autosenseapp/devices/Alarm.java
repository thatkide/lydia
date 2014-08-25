//package com.autosenseapp.lydia.devices;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.preference.PreferenceManager;
//import android.util.Log;
//import com.autosenseapp.lydia.databases.RFIDTag;
//import com.autosenseapp.lydia.interfaces.SerialIO;
//import ca.efriesen.lydia_common.includes.Constants;
//import ca.efriesen.lydia_common.includes.Intents;
////import com.hoho.android.usbserial.util.SerialInputOutputManager;
//import java.util.ArrayList;
//
///**
// * Created by eric on 2013-08-15.
// */
//public class Alarm extends Device implements SerialIO {
//	private static final String TAG = "lydia alarm";
//	private static final int ID = 14;
//
//	private Context context;
////	private SerialInputOutputManager serialInputOutputManager = null;
//
//	public Alarm(Context context, int id, String intentFilter) {
//		super(context, id, intentFilter);
//		this.context = context;
//	}
//
//	@Override
//	public void initialize() {
//		// check the preferences if we have auto find slaves turned on
//		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("autoFindSlaveDevices", true)) {
//			for (int i=0; i<3; i++) {
//				// we do, so send a ping request to the alarm module
//				byte[] pingRequest = {Constants.PINGREQUEST, ID};
//				write(pingRequest);
//				try {
//					Thread.sleep(10);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			// set the alarm to off, if we get a reply, we'll turn it back on
//			PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("useAlarmModule", false).apply();
//		}
//
//		context.registerReceiver(writeAlarmSettingsReceiver, new IntentFilter(Intents.ALARM));
//	}
//
//	@Override
//	public void cleanUp() {
//		try {
//			context.unregisterReceiver(writeAlarmSettingsReceiver);
//		} catch (Exception e) {}
//	}
//
//	@Override
//	public void setIOManager(Object serialInputOutputManager) {
////		this.serialInputOutputManager = (SerialInputOutputManager) serialInputOutputManager;
//	}
//
//	@Override
//	public void write(byte[] command) {
//		try {
//			// write the bytes to the arduino
////			serialInputOutputManager.writeAsync(command);
//		} catch (Exception e) {}
//	}
//
//	@Override
//	public void setValue(ArrayList<String> commands) {
//		// get the value sent from the alarm
//		// commands index 0 is the module
//		int value = Integer.parseInt(commands.get(1));
//		switch (value) {
//			case Constants.PINGREPLY: {
//				PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("useAlarmModule", true).apply();
//				break;
//			}
//			case Constants.RFIDNUMBER: {
//				// get the three bytes from the master, and bit shift them into place, then spit out the number
//				long cardNum = 0;
//				cardNum |= Integer.parseInt(commands.get(2)) & 0xFF;
//				cardNum <<= 8;
//				cardNum |= Integer.parseInt(commands.get(3)) & 0xFF;
//				cardNum <<= 8;
//				cardNum |= Integer.parseInt(commands.get(4)) & 0xFF;
//				// currently the only thing that receives this intent is the rfid setup class.  it uses this to add new tags to the db
//				context.sendBroadcast(new Intent(Intents.RFID).putExtra(Intents.RFID, cardNum));
//				break;
//			}
//		}
//	}
//
//	private BroadcastReceiver writeAlarmSettingsReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (intent.hasExtra("rfid_tag")) {
//				// rfid tag has id, tag number, enabled, start car, open doors and description.
//				// mcu eeprom needs number, enabled, start car, and open doors
//				// 4 bytes.  first three are tag number, last 1 can store the three boolean vars (B00000111)
//				// the first one is 0-255.  since we have 1024 bytes, and each tag uses 4, that gives us a total of 256 possible tags if we don't use eeprom for anything else.
//				// in actuality i don't see needing more than 5 or 10 spots.  however, to be flexible, let's say we use 25.  that is 100 bytes of eeprom space reserved, leaving 924 bytes available.
//				// let's use the first 100.
//				// we have 1024 bytes to play with.
//				RFIDTag tag = (RFIDTag) intent.getSerializableExtra("rfid_tag");
//
//				int eepromAddress = tag.getEepromAddress();
//				int high, med , low;
//				byte flags = 0;
//
//				long cardNum = tag.getTagNumber();
//
//				low = (int) (cardNum & 0xFF);
//				med = (int) ((cardNum >> 8) & 0xFF);
//				high = (int) ((cardNum >> 16) & 0xFF);
//
//				// enabled is bit 0 (1), door is bit 1 (2), and start is bit 2 (4)
//				if (tag.getEnabled()) {	flags += 1; }
//				if (tag.getUnlockDoors()) {	flags += 2; }
//				if (tag.getStartCar()) { flags += 4; }
//
//				Log.d(TAG, "high " + high + " med " + med + " low " + low + " flags " + flags);
//				// send to the alarm, the address, high, med, and low bytes for the card number, and the boolean switches stored in binary format
//				byte[] data = {Constants.ALARM, Constants.EEPROM, (byte)eepromAddress, (byte)high, (byte)med, (byte)low, flags};
//				write(data);
//
//			} else if (intent.hasExtra("autoArm")) {
//				byte[] data = {Constants.ALARM, Constants.AUTOARM, (intent.getBooleanExtra("autoArm", true) ? (byte)0 : (byte)1)};
//				write(data);
//
//			} else if (intent.hasExtra("autoArmDelay")) {
//				int delay = intent.getIntExtra("autoArmDelay", 30);
//				Log.d(TAG, "auto arm delay is " + delay);
//				byte[] delayBytes = {(byte)(delay & 0xFF), (byte) ((delay >> 8) & 0xFF) };
//
//				// combine everything
//				byte[] data = {Constants.ALARM, Constants.AUTOARMDELAY, delayBytes[0], delayBytes[1]};
//				// write it out
//				write(data);
//			}
//		}
//	};
//}
