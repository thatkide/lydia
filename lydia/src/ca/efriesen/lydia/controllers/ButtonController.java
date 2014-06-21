package ca.efriesen.lydia.controllers;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.settings.ButtonEditor;
import ca.efriesen.lydia.activities.settings.HomeScreenActivityCallback;
import ca.efriesen.lydia.controllers.ButtonControllers.*;
import ca.efriesen.lydia.databases.ButtonConfigDataSource;

import java.util.*;

/**
 * Created by eric on 2014-06-14.
 */
public class ButtonController implements View.OnClickListener, View.OnLongClickListener, View.OnDragListener {

	private final static String TAG = "Lydia button controller";
	private Activity activity;
	public HomeScreenActivityCallback homeScreenActivityCallback;
	private ButtonConfigDataSource dataSource;

	// create a new hashmap that takes the action and maps it to a class
	private Map<String, BaseButton> buttons = new HashMap<String, BaseButton>();
	private boolean admin = false;

	// request code for edit activity
	public static final int EDIT_BUTTON = 1;

	public ButtonController(Activity activity) {
		this.activity = activity;
		dataSource = new ButtonConfigDataSource(activity);
		// add all the possible actions and classes
		buttons.put(AirRideButton.ACTION, new AirRideButton(activity));
		buttons.put(AndroidButton.ACTION, new AndroidButton(activity));
		buttons.put(CalendarButton.ACTION, new CalendarButton(activity));
		buttons.put(ChromeButton.ACTION, new ChromeButton(activity));
		buttons.put(ContactsButton.ACTION, new ContactsButton(activity));
		buttons.put(EngineStatusButton.ACTION, new EngineStatusButton(activity));
		buttons.put(MusicButton.ACTION, new MusicButton(activity));
		buttons.put(NavigationButton.ACTION, new NavigationButton(activity));
		buttons.put(PhoneButton.ACTION, new PhoneButton(activity));
		buttons.put(SettingsButton.ACTION, new SettingsButton(activity));
		buttons.put(VideosButton.ACTION, new VideosButton(activity));
		buttons.put(WeatherButton.ACTION, new WeatherButton(activity));
	}

	public ArrayList<BaseButton> getButtons() {
		ArrayList<BaseButton> list = new ArrayList<BaseButton>(buttons.values());

		// sort alphabetically
		Collections.sort(list, new Comparator<BaseButton>() {
			@Override
			public int compare(BaseButton o, BaseButton o2) {
				return o.getDescription().compareToIgnoreCase(o2.getDescription());
			}
		});
		return list;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	@Override
	public void onClick(View view) {
		try {
			// get the bundle of the button pressed
			ca.efriesen.lydia.databases.Button passedButton = (ca.efriesen.lydia.databases.Button) view.getTag();
			// non admin mode, do what's defined in the button class
			if (!admin) {
				// get the button from the hashmap, and execute the onclick method
				BaseButton button = buttons.get(passedButton.getAction());
				button.onClick();
			// admin mode, edit the button
			} else {
				// start the edit button activity.  it will return a result, which will be passed back here to onActivityResult, which will update the UI
				activity.startActivityForResult(new Intent(activity, ButtonEditor.class).putExtra("button", passedButton), EDIT_BUTTON);
			}
		} catch (Exception e) {}
	}

	@Override
	public boolean onLongClick(View view) {
		try {
			// get the bundle of the button pressed
			ca.efriesen.lydia.databases.Button passedButton = (ca.efriesen.lydia.databases.Button) view.getTag();
			// non admin mode, do what's defined in the button class
			if (!admin) {
				// get the button from the hashmap, and execute the onclick method
				BaseButton button = buttons.get(passedButton.getAction());
				button.onLongClick();
			// admin mode, remove the button
			} else {
//				removeButton(passedButton);
				ClipData data = ClipData.newPlainText("", "");
				View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
				view.startDrag(data, shadowBuilder, view, 0);
				view.setVisibility(View.INVISIBLE);
				view.setTag(passedButton);
			}
		} catch (Exception e) {}
		// we've handled the click.
		return true;
	}

	public boolean hasValidSettingsButton() {
		dataSource.open();
		boolean hasButton = dataSource.hasSettingsButton();
		dataSource.close();
		return hasButton;
	}

	public void cleanup() {
		for (Map.Entry<String , BaseButton>entry : buttons.entrySet()) {
			BaseButton button = entry.getValue();
			button.cleanUp();
		}
	}

	public void clearButtons() {
		String idName = "home";
		if (admin) {
			idName = "settings_home";
		}

		for (int i=0; i<6; i++) {
			int resId = activity.getResources().getIdentifier(idName + i, "id", activity.getPackageName());
			// get the button
			Button button = (Button) activity.findViewById(resId);
			// remove the text
			button.setText("");
			// remove the image
			button.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			// nullify the tag
			button.setTag(null);
		}
	}


	public void populateButton(List<ca.efriesen.lydia.databases.Button> buttons) {
		populateButton(buttons, 1);
	}

	// the area being passed is only for the admin activity.  it passes it up to the button editor so the button appears on the proper screen
	public void populateButton(List<ca.efriesen.lydia.databases.Button> buttons, int area) {
		int numButtons = 6;
		String idName = "home";
		if (admin) {
			idName = "settings_home";
		}

		clearButtons();

		if (admin) {
			// set defaults, override them after
			for (int i = 0; i < numButtons; i++) {
				int resId = activity.getResources().getIdentifier(idName + i, "id", activity.getPackageName());
				// get the button
				Button button = (Button) activity.findViewById(resId);
				ca.efriesen.lydia.databases.Button myButton = new ca.efriesen.lydia.databases.Button();
				myButton.setDisplayArea(area);
				myButton.setPosition(i);
				button.setTag(myButton);
				button.setOnDragListener(this);
			}
		}

		// loop over all buttons and populate accordingly
		for (int i=0; i<buttons.size(); i++) {
			// get the bundle of info
			ca.efriesen.lydia.databases.Button myButton = buttons.get(i);
			// get the resource id for the button
			int resId = activity.getResources().getIdentifier(idName + myButton.getPosition(), "id", activity.getPackageName());
			// get the button
			Button button = (Button) activity.findViewById(resId);
			// set the text to the proper title
			button.setText(myButton.getTitle());
			// get the image resource id
			int imgId = activity.getResources().getIdentifier(myButton.getDrawable(), "drawable", activity.getPackageName());
			// get the drawable
			Drawable img = activity.getResources().getDrawable(imgId);
			// set it to the top on the button
			button.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
			button.setTag(myButton);
		}
	}

	// remove the button from the db, and do the callback
	public void removeButton(ca.efriesen.lydia.databases.Button button) {
		// open the db
		dataSource.open();
		// remove the button
		dataSource.removeButton(button);
		// get all the remaining buttons
		List<ca.efriesen.lydia.databases.Button> buttons = dataSource.getButtonsInArea(button.getDisplayArea());
		// close the db
		dataSource.close();

		try {
			// send the new data back
			homeScreenActivityCallback.onLayoutChange(buttons);
		} catch (Exception e) {
			// No callback defined, do nothing
		}
	}

	// the admin home screen editor will call this method
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EDIT_BUTTON && resultCode == Activity.RESULT_OK) {
			ca.efriesen.lydia.databases.Button button = data.getParcelableExtra("button");
			// open the db
			dataSource.open();
			// get all the remaining buttons
			List<ca.efriesen.lydia.databases.Button> buttons = dataSource.getButtonsInArea(button.getDisplayArea());
			// close the db
			dataSource.close();

			try {
				// send the new data back
				homeScreenActivityCallback.onLayoutChange(buttons);
			} catch (Exception e) {
				// No callback defined, do nothing
			}
		}
	}

	// we'll use this drag listener for button rearranging
	@Override
	public boolean onDrag(View view, DragEvent dragEvent) {
		View v = (View) dragEvent.getLocalState();
		ca.efriesen.lydia.databases.Button button = (ca.efriesen.lydia.databases.Button) v.getTag();
		switch (dragEvent.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED: {
				break;
			}
			case DragEvent.ACTION_DRAG_ENTERED: {
				// get the proper background resource
				int pos = ((ca.efriesen.lydia.databases.Button)view.getTag()).getPosition();
				Drawable bg = getButtonBgFromPos(pos);
				// filter it to be lighter
				bg.setColorFilter(new LightingColorFilter(Color.argb(155, 75, 75, 75), Color.argb(155, 75, 75, 75)));
				// set it
				view.setBackground(bg);
				break;
			}
			case DragEvent.ACTION_DRAG_EXITED: {
				// set the original background back
				int pos = ((ca.efriesen.lydia.databases.Button)view.getTag()).getPosition();
				view.setBackground(getButtonBgFromPos(pos));
				break;
			}
			case DragEvent.ACTION_DROP: {
				// set the original background back
				int pos = ((ca.efriesen.lydia.databases.Button)view.getTag()).getPosition();
				view.setBackground(getButtonBgFromPos(pos));

				// open the datasouce, we'll switch buttons, and get the updated list in one go
				dataSource.open();
				// but this button in this position, switching the two
				dataSource.switchButtons(button, pos);
				// get the buttons in our area
				List<ca.efriesen.lydia.databases.Button> buttons = dataSource.getButtonsInArea(button.getDisplayArea());
				// close the db, we don't need it any more
				dataSource.close();

				// this draws the buttons that are actually populated
				populateButton(buttons, button.getDisplayArea());

				break;
			}
			case DragEvent.ACTION_DRAG_ENDED: {
				break;
			}
		}
		return true;
	}

	private Drawable getButtonBgFromPos(int pos) {
		Drawable background;
		switch (pos) {
			case 0: {
				background = activity.getResources().getDrawable(R.drawable.button_bg_round_top_left);
				break;
			}
			case 2: {
				background = activity.getResources().getDrawable(R.drawable.button_bg_round_top_right);
				break;
			}
			case 3: {
				background = activity.getResources().getDrawable(R.drawable.button_bg_round_bottom_left);
				break;
			}
			case 5: {
				background = activity.getResources().getDrawable(R.drawable.button_bg_round_bottom_right);
				break;
			}
			default: {
				background = activity.getResources().getDrawable(R.drawable.button_bg);
			}
		}
		return background;
	}
}
