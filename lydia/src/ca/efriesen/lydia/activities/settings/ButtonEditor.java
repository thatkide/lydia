package ca.efriesen.lydia.activities.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import android.widget.Button;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.controllers.ButtonController;
import ca.efriesen.lydia.controllers.ButtonControllers.BaseButton;
import ca.efriesen.lydia.databases.*;

import java.util.ArrayList;

/**
 * Created by eric on 2014-06-14.
 */
public class ButtonEditor extends Activity implements View.OnClickListener {

	private static final String TAG = "button editor";

	public static final int ICON_SELECTOR = 1;
	private ca.efriesen.lydia.databases.Button button;
	private Button buttonView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.button_editor);

		// stop keyboard from auto popping up
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		button = getIntent().getParcelableExtra("button");

		TextView title = (TextView) findViewById(R.id.button_title_text);
		buttonView = (Button) findViewById(R.id.button_background);

		// we might not have a title or drawable...
		try {
			title.setText(button.getTitle());

			int imgId = getResources().getIdentifier(button.getDrawable(), "drawable", getPackageName());
			// get the drawable
			Drawable img = getResources().getDrawable(imgId);
			// set it to the top on the button
			buttonView.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
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
		ButtonController buttonController = new ButtonController(this);
		// get an array list of all buttons available
		ArrayList<BaseButton> buttons = buttonController.getButtons();
		// find the spinner
		Spinner activitySpinner = (Spinner) findViewById(R.id.button_edit_spinner);
		// create a new adapter using the button descriptions (using toString() in classes)
		ArrayAdapter<BaseButton> adapter = new ArrayAdapter<BaseButton>(this, android.R.layout.simple_spinner_dropdown_item, buttons);
		// set the adapter
		activitySpinner.setAdapter(adapter);

		// we might not have an action...
		try {
			// set current action
			for (int i = 0; i < adapter.getCount(); i++) {
				if (adapter.getItem(i).getAction().equalsIgnoreCase(button.getAction())) {
					activitySpinner.setSelection(i);
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
		EditText editText = (EditText) findViewById(R.id.button_title_text);
		// save button stuff
		button.setTitle(editText.getText().toString());

		Spinner activitySpinner = (Spinner) findViewById(R.id.button_edit_spinner);
		button.setAction(((BaseButton) activitySpinner.getSelectedItem()).getAction());

		button.setUsesDrawable(true);
		if (button.getDrawable() == null) {
			button.setDrawable("vinyl");
		}

		ButtonConfigDataSource dataSource = new ButtonConfigDataSource(getApplicationContext());
		dataSource.open();
		dataSource.editButton(button);
		dataSource.close();
		Intent returnIntent = new Intent();
		returnIntent.putExtra("button", button);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
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