package ca.efriesen.lydia.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.Window;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.controllers.ButtonController;
import ca.efriesen.lydia.databases.ButtonConfigDataSource;

import java.util.List;

/**
 * Created by eric on 2014-06-14.
 */
public class HomeScreenEditorActivity extends Activity implements View.OnClickListener, View.OnDragListener {

	private static final String TAG = "homescreen editor";

	private ButtonController buttonController;
	private SharedPreferences sharedPreferences;
	private int numHomeScreens;
	// always start on screen 1
	private int selectedScreen = 0;

	private Button prev;
	private Button delete;
	private Button addNew;
	private Button next;
	private RadioGroup radioGroup;
	private LinearLayout buttonDeleteZone;
	private ImageView buttonDeleteImage;
	private TextView buttonDeleteText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.home_screen_layout);

		// we'll store basic info in shared prefs, and more complicated info in sqlite
		sharedPreferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);

		buttonController = new ButtonController(this);
		buttonController.setAdmin(true);

		int numButtons = 6;
		numHomeScreens = sharedPreferences.getInt("numHomeScreens", 1);

		prev = (Button) findViewById(R.id.button_prev_screen);
		delete = (Button) findViewById(R.id.button_delete_screen);
		addNew = (Button) findViewById(R.id.button_add_screen);
		next = (Button) findViewById(R.id.button_next_screen);
		radioGroup = (RadioGroup) findViewById(R.id.radio_button_container);
		buttonDeleteZone = (LinearLayout) findViewById(R.id.button_delete_zone);
		buttonDeleteImage = (ImageView) findViewById(R.id.button_delete_image);
		buttonDeleteText = (TextView) findViewById(R.id.button_delete_text);

		buttonDeleteZone.setOnDragListener(this);

		// draw radio buttons
		for (int i=0; i<numHomeScreens; i++) {
			RadioButton radioButton = new RadioButton(this);
			radioButton.setId(i);
			if (i == 0) {
				radioButton.setChecked(true);
			}
			radioGroup.addView(radioButton);
		}

		drawScreen();

		// control buttons click here
		addNew.setOnClickListener(this);
		delete.setOnClickListener(this);
		next.setOnClickListener(this);
		prev.setOnClickListener(this);

		// tell every button to call the button controller, it will decide your fate
		for (int i=0; i<numButtons; i++) {
			// get the resource id for the button
			int resId = getResources().getIdentifier("settings_home" + i, "id", getPackageName());
			// get the button
			Button button = (Button) findViewById(resId);
			button.setOnClickListener(buttonController);
			button.setOnLongClickListener(buttonController);
		}

		// provide a callback to refresh the buttons once the long click has finished deleting the button from the db
		buttonController.homeScreenActivityCallback = new HomeScreenActivityCallback() {
			@Override
			public void onLayoutChange(List<ca.efriesen.lydia.databases.Button> buttons) {
				// refresh the onscreen display
				buttonController.clearButtons();
				buttonController.populateButton(buttons, selectedScreen);
			}
		};
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		buttonController.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.button_add_screen: {
				sharedPreferences.edit().putInt("numHomeScreens", ++numHomeScreens).apply();
				RadioButton radioButton = new RadioButton(this);
				radioButton.setId(numHomeScreens);
				radioGroup.addView(radioButton);
				drawScreen();
				break;
			}
			case R.id.button_delete_screen: {
				if (numHomeScreens > 1) {
					radioGroup.removeViewAt(selectedScreen);
					// remove the screen from the db and update the others
					ButtonConfigDataSource dataSource = new ButtonConfigDataSource(this);
					dataSource.open();
					dataSource.removeHomeScreen(selectedScreen, numHomeScreens);
					// close the db, we don't need it any more
					dataSource.close();

					sharedPreferences.edit().putInt("numHomeScreens", --numHomeScreens).apply();
				}
				// if we've deleted the last screen, set selected to one less
				if (selectedScreen >= numHomeScreens) {
					selectedScreen = numHomeScreens-1;
				}
				drawScreen();
				break;
			}
			case R.id.button_next_screen: {
				selectedScreen++;
				drawScreen();
				break;
			}
			case R.id.button_prev_screen: {
				selectedScreen--;
				drawScreen();
				break;
			}
		}
	}

	private void drawScreen() {
		ButtonConfigDataSource dataSource = new ButtonConfigDataSource(this);
		dataSource.open();
		// get the buttons in our area
		List<ca.efriesen.lydia.databases.Button> buttons = dataSource.getButtonsInArea(selectedScreen);
		// close the db, we don't need it any more
		dataSource.close();

		// this draws the buttons that are actually populated
		buttonController.populateButton(buttons, selectedScreen);

		// start off with add buttons hidden
		int prevVis = View.INVISIBLE;
		int nextVis = View.INVISIBLE;
		int delVis = View.INVISIBLE;

		// if we have more than one home screen, enable next and delete
		if (numHomeScreens > 1) {
			nextVis = View.VISIBLE;
			delVis = View.VISIBLE;
		}

		// if our selected screen isn't the first, show the previous button
		if (selectedScreen != 0) {
			prevVis = View.VISIBLE;
		}

		//if we're on the last scree, rehide next
		if (selectedScreen == numHomeScreens-1) {
			nextVis = View.INVISIBLE;
		}

		// set the visibility
		prev.setVisibility(prevVis);
		next.setVisibility(nextVis);
		delete.setVisibility(delVis);

		RadioButton button = (RadioButton) radioGroup.getChildAt(selectedScreen);
		button.setChecked(true);
	}

	@Override
	public boolean onDrag(View view, DragEvent dragEvent) {
		View v = (View) dragEvent.getLocalState();
		ca.efriesen.lydia.databases.Button button = (ca.efriesen.lydia.databases.Button) v.getTag();
		switch (dragEvent.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED: {
				buttonDeleteImage.setVisibility(View.VISIBLE);
				buttonDeleteText.setVisibility(View.VISIBLE);
				break;
			}
			case DragEvent.ACTION_DRAG_ENTERED: {
				buttonDeleteText.setTextColor(Color.RED);
				buttonDeleteImage.setColorFilter(Color.RED);

				break;
			}
			case DragEvent.ACTION_DRAG_EXITED: {
				buttonDeleteText.setTextColor(Color.WHITE);
				buttonDeleteImage.setColorFilter(Color.WHITE);
				break;
			}
			case DragEvent.ACTION_DROP: {
				buttonController.removeButton(button);
				// set the original button space back to visible
				v.setVisibility(View.VISIBLE);
				// redraw all buttons
				drawScreen();
				break;
			}
			case DragEvent.ACTION_DRAG_ENDED: {
				// reset these back to white
				buttonDeleteText.setTextColor(Color.WHITE);
				buttonDeleteImage.setColorFilter(Color.WHITE);
				buttonDeleteImage.setVisibility(View.GONE);
				buttonDeleteText.setVisibility(View.GONE);
				// show button after drop if not removed
				v.setVisibility(View.VISIBLE);
				break;
			}
		}
		return true;
	}
}