package ca.efriesen.lydia.activities.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import android.widget.Button;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.controllers.ButtonController;
import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.databases.*;
import ca.efriesen.lydia.includes.AppInfo;

import java.util.ArrayList;

/**
 * Created by eric on 2014-06-14.
 */
public class ButtonEditor extends Activity implements View.OnClickListener {

	private static final String TAG = ButtonEditor.class.getSimpleName();

	public static final int ICON_SELECTOR = 1;
	private ca.efriesen.lydia.databases.Button button;
	private Button buttonView;
	private int buttonType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.button_editor);

		// stop keyboard from auto popping up
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		button = getIntent().getParcelableExtra("button");
		buttonType = getIntent().getIntExtra("buttonType", BaseButton.TYPE_HOMESCREEN);

		final TextView title = (TextView) findViewById(R.id.button_title_text);
		buttonView = (Button) findViewById(R.id.button_background);

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
			title.setText(button.getTitle());
			if (button.getDrawable() != null) {
				int imgId = this.getResources().getIdentifier(button.getDrawable(), "drawable", this.getPackageName());
				// get the drawable
				Drawable img = getResources().getDrawable(imgId);
				// set it to the top on the button
				buttonView.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
			}
		} catch (NullPointerException e) {
		}

		Button ok = (Button) findViewById(R.id.button_edit_ok);
		ok.setOnClickListener(this);

		Button cancel = (Button) findViewById(R.id.button_edit_cancel);
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
		// find the spinner
		final Spinner actionSpinner = (Spinner) findViewById(R.id.button_action_spinner);
		final Spinner extraDataSpinner = (Spinner) findViewById(R.id.button_extra_data_spinner);
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

					extraDataSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
							Object selectedItem = extraDataSpinner.getSelectedItem();
							if (selectedItem instanceof AppInfo) {
								Drawable drawable = ((AppInfo) selectedItem).getIcon();
								Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
								buttonView.setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable(getApplicationContext().getResources(), Bitmap.createScaledBitmap(bitmap, 100, 100, true)), null, null);
								title.setText(((AppInfo) selectedItem).getAppName());
							}
						}

						@Override
						public void onNothingSelected(AdapterView<?> parent) {

						}
					});
				} else {
					extraDataSpinner.setVisibility(View.GONE);
				}
				// only set the text if the button doesn't have any
				if (button.getTitle() == null || button.getTitle().isEmpty()) {
					// set the title to the default string
					TextView title = (TextView) findViewById(R.id.button_title_text);
					title.setText(baseButton.getDefaultName());
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
		Spinner actionSpinner = (Spinner) findViewById(R.id.button_action_spinner);
		Spinner extraDataSpinner = (Spinner) findViewById(R.id.button_extra_data_spinner);
		EditText editText = (EditText) findViewById(R.id.button_title_text);
		BaseButton baseButton = (BaseButton) actionSpinner.getSelectedItem();

		// save button stuff
		button.setTitle(editText.getText().toString());
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