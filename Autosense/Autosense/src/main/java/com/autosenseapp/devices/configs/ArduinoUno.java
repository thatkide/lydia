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
public class ArduinoUno implements ArduinoConfig {

	private static final String TAG = ArduinoDue.class.getSimpleName();

	private static final int BLACK = -13226195;
	private static final int RED = -65536;
	private static final int GREEN = -16711936;
	private static final int YELLOW = -256;

	private Activity activity;

	private HashMap<Integer, Set<String>> greenMap;
	private HashMap<Integer, Set<String>> redMap;
	private HashMap<Integer, Set<String>> yellowMap;

	public ArduinoUno(Activity activity) {
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

		// Analog pins. We make them negative numbers to differentiate them
		yellowMap = new HashMap<Integer, Set<String>>();

		for (int i=3; i>=0; i--) {
			yellowMap.put((i*-1), new HashSet<String>(Arrays.asList("A" + i)));
		}
		yellowMap.put(-5, new HashSet<String>(Arrays.asList("A5", "(SCL)")));
		yellowMap.put(-4, new HashSet<String>(Arrays.asList("A4", "(SDA)")));


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
			// A0 - A7
			case YELLOW: {
				intent.putExtra("pins", yellowMap);
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
