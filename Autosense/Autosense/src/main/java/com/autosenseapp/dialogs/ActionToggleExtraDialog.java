package com.autosenseapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import com.autosenseapp.AutosenseApplication;
import com.autosenseapp.R;
import com.autosenseapp.databases.ArduinoPin;
import com.autosenseapp.devices.actions.ActionToggle;
import javax.inject.Inject;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.widget.AmbilWarnaPrefWidgetView;

/**
 * Created by eric on 2014-10-04.
 */
public class ActionToggleExtraDialog extends Dialog {

	@InjectView(R.id.high_color_view) AmbilWarnaPrefWidgetView highWidgetView;
	@InjectView(R.id.low_color_view) AmbilWarnaPrefWidgetView lowWidgetView;
	@Inject SharedPreferences sharedPreferences;
	private ArduinoPin arduinoPin;
	private int highColor;
	private int lowColor;

	public ActionToggleExtraDialog(Context context, ArduinoPin arduinoPin) {
		super(context);
		this.arduinoPin = arduinoPin;
	}

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		((AutosenseApplication)getContext().getApplicationContext()).inject(this);
		setContentView(R.layout.dialog_toggle_extras);
		ButterKnife.inject(this);
		highColor = sharedPreferences.getInt(ActionToggle.PREFHIGH + arduinoPin.getId(), Color.WHITE);
		lowColor = sharedPreferences.getInt(ActionToggle.PREFLOW + arduinoPin.getId(), Color.WHITE);

		highWidgetView.setBackgroundColor(highColor);
		lowWidgetView.setBackgroundColor(lowColor);
	}

	@OnClick(R.id.high_row)
	public void clickHigh() {
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(getContext(), highColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
			@Override
			public void onCancel(AmbilWarnaDialog dialog) { }

			@Override
			public void onOk(AmbilWarnaDialog dialog, int color) {
				sharedPreferences.edit().putInt(ActionToggle.PREFHIGH + arduinoPin.getId(), color).apply();
				highColor = color;
				highWidgetView.setBackgroundColor(color);
			}
		});
		dialog.show();
	}

	@OnClick(R.id.low_row)
	public void clickLow() {
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(getContext(), lowColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
			@Override
			public void onCancel(AmbilWarnaDialog dialog) { }

			@Override
			public void onOk(AmbilWarnaDialog dialog, int color) {
				sharedPreferences.edit().putInt(ActionToggle.PREFLOW + arduinoPin.getId(), color).apply();
				lowColor = color;
				lowWidgetView.setBackgroundColor(color);
			}
		});
		dialog.show();
	}
}