//package com.autosenseapp.lydia.devices;
//
//import android.content.Context;
//import android.content.Intent;
//import com.autosenseapp.lydia.interfaces.SerialIO;
//import ca.efriesen.lydia_common.includes.Constants;
//import ca.efriesen.lydia_common.includes.Intents;
////import com.hoho.android.usbserial.util.SerialInputOutputManager;
//import java.util.ArrayList;
//
///**
// * Created by eric on 2013-09-08.
// */
//public class MJLJReceiver extends Device implements SerialIO {
//	private Context context;
////	private SerialInputOutputManager serialInputOutputManager = null;
//
//	private MJLJ mjlj = new MJLJ();
//
//	public MJLJReceiver(Context context, int id, String intentFilter) {
//		super(context, id, intentFilter);
//		this.context = context;
//
//	}
//
//	@Override
//	public void cleanUp() {
//	}
//
//	@Override
//	public void setValue(ArrayList<String> commands) {
//		int command = Integer.parseInt(commands.get(1)); // get the numerical command
//		String value = commands.get(2);
//
//		switch (command) {
//			case Constants.GETADVANCE: {
//				mjlj.setAdvance(value);
//				break;
//			}
//			case Constants.GETFLAGS: {
//				mjlj.setFlags(value);
//				break;
//			}
//			case Constants.GETLOAD: {
//				mjlj.setLoad(String.valueOf(Integer.parseInt(value) * 7));
//				break;
//			}
//			case Constants.GETRPM: {
//				int lowByte = Integer.parseInt(commands.get(3));
//				int highByte = Integer.parseInt(value);
//
//				int word = 0;
//				word |= highByte & 0xFF;
//				word <<= 8;
//				word |= lowByte & 0xFF;
//				value = String.valueOf(word);
//
//				mjlj.setRpm(value);
//				break;
//			}
//			case Constants.GETRUNNING: {
//				mjlj.setRunning(Integer.parseInt(value) > 0);
//				break;
//			}
//		}
//		context.sendBroadcast(new Intent(Intents.MJLJ).putExtra(Intents.MJLJ, mjlj));
//	}
//
//	@Override
//	public void setIOManager(Object serialInputOutputManager) {
////		this.serialInputOutputManager = (SerialInputOutputManager) serialInputOutputManager;
//	}
//
//	@Override
//	public void write(byte[] command) {
////		try {
//			// write the bytes to the arduino
////			serialInputOutputManager.writeAsync(command);
////		} catch (Exception e) {}
//	}
//
//}
