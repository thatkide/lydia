package com.autosenseapp.activities.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.*;
import android.widget.Button;
import com.autosenseapp.R;
import com.autosenseapp.callbacks.DrawScreenCallback;
import com.autosenseapp.controllers.ButtonController;
import com.autosenseapp.buttons.BaseButton;
import java.util.List;

/**
 * Created by eric on 2014-06-14.
 */
public class HomeScreenEditorActivity extends Activity implements View.OnClickListener, DrawScreenCallback {

	private static final String TAG = HomeScreenEditorActivity.class.getSimpleName();

	private ButtonController buttonController;

	public static final String BASENAME = "home_";

	private Button prev;
	private Button delete;
	private Button addNew;
	private Button next;
	private RadioGroup radioGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.home_screen_layout_editor);

		buttonController = new ButtonController(this, BASENAME, BaseButton.TYPE_HOMESCREEN, BaseButton.BUTTONS_PER_HOMESCREEN, true, BaseButton.GROUP_USER);

		// get all the view objects needed
		prev = (Button) findViewById(R.id.button_prev_screen);
		delete = (Button) findViewById(R.id.button_delete_screen);
		addNew = (Button) findViewById(R.id.button_add_screen);
		next = (Button) findViewById(R.id.button_next_screen);
		radioGroup = (RadioGroup) findViewById(R.id.radio_button_container);

		// draw radio buttons
		for (int i=0; i<buttonController.getNumScreens(); i++) {
			RadioButton radioButton = new RadioButton(this);
			radioButton.setId(i);
			// set the first one checked
			if (i == 0) {
				radioButton.setChecked(true);
			}
			// add the buttons
			radioGroup.addView(radioButton);
		}

		// draw all the icons
		buttonController.drawScreen();

		// set the control button tags and tell them to onclick via the button controller
		addNew.setTag(BaseButton.BUTTON_SCREEN_ADD);
		addNew.setOnClickListener(buttonController);
		delete.setTag(BaseButton.BUTTON_SCREEN_DELETE);
		delete.setOnClickListener(buttonController);
		next.setTag(BaseButton.BUTTON_NEXT);
		next.setOnClickListener(buttonController);
		prev.setTag(BaseButton.BUTTON_PREV);
		prev.setOnClickListener(buttonController);

		// tell every icon button to call the button controller, it will decide your fate
		for (int i=0; i<BaseButton.BUTTONS_PER_HOMESCREEN; i++) {
			// get the resource id for the button
			int resId = getResources().getIdentifier(BASENAME + i, "id", getPackageName());
			// get the button
			Button button = (Button) findViewById(resId);
			button.setOnClickListener(buttonController);
			button.setOnLongClickListener(buttonController);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		buttonController.onActivityResult(requestCode, resultCode, data);
	}

	// implement an onclick listener so we can update the radio buttons
	// if we didn't have the radio butons we wouldn't need to include this onclick.  the button controller would do all the work we need
	@Override
	public void onClick(View view) {
		if (view.getTag() instanceof Integer) {
			switch ((Integer) view.getTag()) {
				// if we clicked add, add a new radio button
				case BaseButton.BUTTON_SCREEN_ADD: {
					RadioButton radioButton = new RadioButton(this);
					radioButton.setId(buttonController.getNumScreens());
					radioGroup.addView(radioButton);
					break;
				}
				// delete? remove the selected button
				case BaseButton.BUTTON_SCREEN_DELETE: {
					if (buttonController.getNumScreens() > 1) {
						radioGroup.removeViewAt(buttonController.getSelectedScreen());
					}
					break;
				}
			}
		}
	}

	// include the drawscreen callback.
	// this allows us to show/hide the buttons properly
	@Override
	public void drawScreen(List<com.autosenseapp.databases.Button> buttons) {
		// start off with add buttons hidden
		int prevVis = View.INVISIBLE;
		int nextVis = View.INVISIBLE;
		int delVis = View.INVISIBLE;

		// if we have more than one home screen, enable next and delete
		if (buttonController.getNumScreens() > 1) {
			nextVis = View.VISIBLE;
			delVis = View.VISIBLE;
		}

		// if our selected screen isn't the first, show the previous button
		if (buttonController.getSelectedScreen() != 0) {
			prevVis = View.VISIBLE;
		}

		//if we're on the last scree, rehide next
		if (buttonController.getSelectedScreen() == buttonController.getNumScreens()-1) {
			nextVis = View.INVISIBLE;
		}

		// set the visibility
		prev.setVisibility(prevVis);
		next.setVisibility(nextVis);
		delete.setVisibility(delVis);

		RadioButton button = (RadioButton) radioGroup.getChildAt(buttonController.getSelectedScreen());
//		button.setChecked(true);
	}

	@Override
	public boolean fullDraw() {
		return false;
	}

}