package com.autosenseapp.fragments.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.autosenseapp.fragments.BaseFragment;
import com.autosenseapp.services.ArduinoService;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by eric on 2014-08-29.
 */
public class MasterIoFragment extends BaseFragment implements View.OnTouchListener {

	private static final String TAG = MasterIoFragment.class.getSimpleName();

	@InjectView(R.id.arduino_image_overlay) ImageView overlay;
	@InjectView(R.id.arduino_image) ImageView arduinoImage;
	@Inject SharedPreferences sharedPreferences;

	private ArduinoConfig arduinoConfig;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		super.onCreateView(inflater, container, savedInstance);
		View view = inflater.inflate(R.layout.master_io_prefs, container, false);
		ButterKnife.inject(this, view);
		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// clear the views
		ButterKnife.reset(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		arduinoConfig.onResume();
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		Activity activity = getActivity();

		int arduinoType = sharedPreferences.getInt(ArduinoService.ARDUINO_TYPE, ArduinoService.ARDUINO_NONE);

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
