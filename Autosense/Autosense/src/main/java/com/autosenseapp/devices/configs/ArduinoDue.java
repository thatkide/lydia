package com.autosenseapp.devices.configs;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.autosenseapp.activities.settings.ArduinoPinEditor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by eric on 2014-08-30.
 */
public class ArduinoDue implements ArduinoConfig {

	private static final String TAG = ArduinoDue.class.getSimpleName();

	private static final int BLACK = -13226195;
	private static final int RED = -65536;
	private static final int GREEN = -16711936;
	private static final int BLUE = -16776961;
	private static final int PINK = -1310580;
	private static final int ORANGE = -551907;
	private static final int YELLOW = -256;

	private Activity activity;

	private HashMap<Integer, Set<String>> greenMap;
	private HashMap<Integer, Set<String>> redMap;
	private HashMap<Integer, Set<String>> blueMap;
	private HashMap<Integer, Set<String>> pinkMap;
	private HashMap<Integer, Set<String>> yellowMap;
	private HashMap<Integer, Set<String>> orangeMap;

	public ArduinoDue(Activity activity) {
		this.activity = activity;

		// new map for "green zone"
		greenMap = new HashMap<Integer, Set<String>>();
		greenMap.put(0, new HashSet<String>(Arrays.asList("RX0")));
		greenMap.put(1, new HashSet<String>(Arrays.asList("TX0")));
		// pins 2 - 7 don't have special purpose, just use null
		for (int i=2; i<=7; i++) {
			greenMap.put(i, null);
		}

		// new map for "red zone"
		redMap = new HashMap<Integer, Set<String>>();
		for (int i=8; i<=13; i++) {
			redMap.put(i, null);
		}

		blueMap = new HashMap<Integer, Set<String>>();
		blueMap.put(14, new HashSet<String>(Arrays.asList("TX3")));
		blueMap.put(15, new HashSet<String>(Arrays.asList("RX3")));
		blueMap.put(16, new HashSet<String>(Arrays.asList("TX2")));
		blueMap.put(17, new HashSet<String>(Arrays.asList("RX2")));
		blueMap.put(18, new HashSet<String>(Arrays.asList("TX1")));
		blueMap.put(19, new HashSet<String>(Arrays.asList("RX1")));
		blueMap.put(20, new HashSet<String>(Arrays.asList("SDA")));
		blueMap.put(21, new HashSet<String>(Arrays.asList("SCL")));

		// new map for "pink zone"
		pinkMap = new HashMap<Integer, Set<String>>();
		// nothing special here, just add them
		for (int i=22; i<=53; i++) {
			pinkMap.put(i, null);
		}

		yellowMap = new HashMap<Integer, Set<String>>();

		for (int i=7; i>=0; i--) {
			yellowMap.put((i*-1), new HashSet<String>(Arrays.asList("A" + i)));
		}

		orangeMap = new HashMap<Integer, Set<String>>();

		// Analog pins. We make them negative numbers to differentiate them
		for (int i=11; i>=8; i--) {
			orangeMap.put((i*-1), new HashSet<String>(Arrays.asList("A" + i)));
		}
	}

	@Override
	public void handleClick(int color) {
		Intent intent = new Intent(activity, ArduinoPinEditor.class);
		switch (color) {
			// Reset
			case BLACK: {
				break;
			}
			// 0 - 7
			case GREEN: {
				intent.putExtra("pins", greenMap);
				break;
			}
			// 8 - 13, I2C 1
			case RED: {
				intent.putExtra("pins", redMap);
				break;
			}
			// Communication - 14 - 21
			case BLUE: {
				intent.putExtra("pins", blueMap);
				break;
			}
			// 22 - 53
			case PINK: {
				intent.putExtra("pins", pinkMap);
				break;
			}
			// A0 - A7
			case YELLOW: {
				intent.putExtra("pins", yellowMap);
				break;
			}
			// A8 - A11, DAC0, DAC1, CAN
			case ORANGE: {
				intent.putExtra("pins", orangeMap);
				break;
			}
			default: {
				// no nothing
				return;
			}
		}

		activity.startActivity(intent);

	}
}
