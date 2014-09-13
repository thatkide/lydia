package com.autosenseapp.fragments.Settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.autosenseapp.R;
import com.autosenseapp.devices.configs.ArduinoConfig;
import com.autosenseapp.devices.configs.ArduinoDue;
import com.autosenseapp.devices.configs.ArduinoUno;
import com.autosenseapp.services.ArduinoService;

/**
 * Created by eric on 2014-08-29.
 */
public class MasterIoFragment extends Fragment implements View.OnTouchListener {

	private static final String TAG = MasterIoFragment.class.getSimpleName();

	private ImageView overlay;
	private ArduinoConfig arduinoConfig;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		super.onCreateView(inflater, container, savedInstance);
		return inflater.inflate(R.layout.master_io_prefs, container, false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		Activity activity = getActivity();

		ImageView arduinoImage = (ImageView) activity.findViewById(R.id.arduino_image);
		overlay = (ImageView) activity.findViewById(R.id.arduino_image_overlay);

		int arduinoType = activity.getSharedPreferences(activity.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS).getInt(ArduinoService.ARDUINO_TYPE, ArduinoService.ARDUINO_NONE);

		switch (arduinoType) {
			case ArduinoService.ARDUINO_ACCESSORY: {
				arduinoImage.setImageResource(R.drawable.arduino_due);
				arduinoConfig = new ArduinoDue(activity);
				break;
			}
			case ArduinoService.ARDUINO_DEVICE: {
				arduinoImage.setImageResource(R.drawable.arduino_uno);
				arduinoConfig = new ArduinoUno(activity);
				break;
			}
			default: {
				arduinoImage.setImageResource(android.R.color.transparent);
				overlay.setImageResource(android.R.color.transparent);
			}
		}

		arduinoImage.setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case (MotionEvent.ACTION_DOWN): {
				int touchColor = getHotSpotColor(event.getX(), event.getY());
				arduinoConfig.handleClick(touchColor);
			}
		}
		return false;
	}

	private int getHotSpotColor(float x, float y) {
		overlay.setDrawingCacheEnabled(true);
		Bitmap hotspots = Bitmap.createBitmap(overlay.getDrawingCache());
		overlay.setDrawingCacheEnabled(false);
		return hotspots.getPixel((int)x, (int)y);
	}
}
