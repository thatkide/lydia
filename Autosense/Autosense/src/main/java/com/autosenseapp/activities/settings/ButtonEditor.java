package com.autosenseapp.activities.settings;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import android.widget.Button;
import com.autosenseapp.R;
import com.autosenseapp.activities.BaseActivity;
import com.autosenseapp.controllers.ButtonController;
import com.autosenseapp.buttons.BaseButton;
import com.autosenseapp.databases.*;
import com.autosenseapp.includes.AppInfo;
import com.autosenseapp.interfaces.ExtraDataSpinner;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by eric on 2014-06-14.
 */
public class ButtonEditor extends BaseActivity implements View.OnClickListener {

	private static final String TAG = ButtonEditor.class.getSimpleName();

	public static final int ICON_SELECTOR = 1;
	private com.autosenseapp.databases.Button button;
	@InjectView(R.id.button_background)	Button buttonView;
	@InjectView(R.id.button_title_text) EditText buttonTitleText;
	@InjectView(R.id.button_edit_ok) Button ok;
	@InjectView(R.id.button_edit_cancel) Button cancel;
	@InjectView(R.id.button_action_spinner) Spinner actionSpinner;
	@InjectView(R.id.button_extra_data_spinner) Spinner extraDataSpinner;
	private int buttonType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.button_editor);
		ButterKnife.inject(this);

		// stop keyboard from auto popping up
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		button = getIntent().getParcelableExtra("button");
		buttonType = getIntent().getIntExtra("buttonType", BaseButton.TYPE_HOMESCREEN);

		if (buttonType != BaseButton.TYPE_HOMESCREEN) {
			// disable the click for all but homescreen buttons
			buttonView.setEnabled(false);
			// get the layout params
			ViewGroup.LayoutParams layoutParams = buttonView.getLayoutParams();
			// resize to sidebar button size
			layoutParams.height /= 2;
			// set the new params
			buttonView.setLayoutParams(layoutParams);
		}

		// we might not have a drawable...
		try {
			buttonTitleText.setText(button.getTitle());
			if (button.getDrawable() != null) {
				int imgId = this.getResources().getIdentifier(button.getDrawable(), "drawable", this.getPackageName());
				// get the drawable
				Drawable img = getResources().getDrawable(imgId);
				// set it to the top on the button
				buttonView.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
			}
		} catch (NullPointerException e) {
		}

		ok.setOnClickListener(this);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Just close the activity
				finish();
			}
		});

		// init the button controller
		ButtonController buttonController = new ButtonController(this, "", buttonType);
		// get an array list of all buttons available
		ArrayList<BaseButton> buttonActions = buttonController.getButtonActions();
		// create a new adapter using the button descriptions (using toString() in classes)
		ArrayAdapter<BaseButton> adapter = new ArrayAdapter<BaseButton>(this, android.R.layout.simple_spinner_dropdown_item, buttonActions);
		// set the adapter
		actionSpinner.setAdapter(adapter);
		// if the selected item has extra data, display the second spinner
		actionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				BaseButton baseButton = (BaseButton) actionSpinner.getSelectedItem();
				if (baseButton.hasExtraData()) {
					extraDataSpinner.setVisibility(View.VISIBLE);
					extraDataSpinner.setAdapter(baseButton.getAdapterData());

					for (int j=0; j<extraDataSpinner.getCount(); j++) {
						String info = ((ExtraDataSpinner) extraDataSpinner.getItemAtPosition(j)).getDataInfo();
						if (info.equalsIgnoreCase(button.getExtraData())) {
							extraDataSpinner.setSelection(j);
							break;
						}
					}

					extraDataSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
							Object selectedItem = extraDataSpinner.getSelectedItem();
							if (selectedItem instanceof AppInfo) {
								if (buttonType == BaseButton.TYPE_HOMESCREEN) {
									Drawable drawable = ((AppInfo) selectedItem).getIcon();
									Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
									buttonView.setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable(getApplicationContext().getResources(), Bitmap.createScaledBitmap(bitmap, 100, 100, true)), null, null);
								}
								buttonTitleText.setText(((AppInfo) selectedItem).getAppName());
							} else if (selectedItem instanceof ArduinoPin) {
								if (((ArduinoPin) selectedItem).getComment().equalsIgnoreCase("")) {
									buttonTitleText.setText(String.valueOf(((ArduinoPin) selectedItem).getPinNumber()));
								} else {
									buttonTitleText.setText(((ArduinoPin) selectedItem).getComment());
								}
							}
						}

						@Override
						public void onNothingSelected(AdapterView<?> parent) { }
					});
				} else {
					extraDataSpinner.setVisibility(View.GONE);
				}
				// only set the text if the button doesn't have any
				if (button.getTitle() == null || button.getTitle().isEmpty()) {
					// set the buttonTitleText to the default string
					buttonTitleText.setText(baseButton.getDefaultName());
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {	}
		});

		// we might not have an action...
		try {
			// set current action
			for (int i = 0; i < adapter.getCount(); i++) {
				if (adapter.getItem(i).getAction().equalsIgnoreCase(button.getAction())) {
					actionSpinner.setSelection(i);
					break;
				}
			}
		} catch (NullPointerException e) {
		}

		buttonView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivityForResult(new Intent(getApplicationContext(), IconSelector.class), ICON_SELECTOR);
			}
		});
	}

	// This is for the OK button click.
	@Override
	public void onClick(View view) {
		BaseButton baseButton = (BaseButton) actionSpinner.getSelectedItem();

		// save button stuff
		button.setTitle(buttonTitleText.getText().toString());
		button.setAction(baseButton.getAction());
		button.setExtraData(baseButton.getExtraData(extraDataSpinner.getSelectedItemPosition()));
		if (buttonType == BaseButton.TYPE_HOMESCREEN) {
			button.setUsesDrawable(true);
		} else {
			button.setUsesDrawable(false);
		}
		if (button.getDrawable() == null) {
			button.setDrawable("blank");
		}
		button.setButtonType(buttonType);

		ButtonConfigDataSource dataSource = new ButtonConfigDataSource(getApplicationContext());
		dataSource.open();
		dataSource.editButton(button);
		dataSource.close();
		Intent returnIntent = new Intent();
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ICON_SELECTOR && resultCode == RESULT_OK) {
			button.setDrawable(data.getStringExtra(ButtonConfigOpenHelper.DRAWABLE));
			int imgId = getResources().getIdentifier(button.getDrawable(), "drawable", getPackageName());
			// get the drawable
			Drawable img = getResources().getDrawable(imgId);
			// set it to the top on the button
			buttonView.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
		}
	}
}