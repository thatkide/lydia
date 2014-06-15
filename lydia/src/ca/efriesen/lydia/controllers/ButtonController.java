package ca.efriesen.lydia.controllers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import ca.efriesen.lydia.activities.settings.ButtonEditor;
import ca.efriesen.lydia.activities.settings.HomeScreenActivityCallback;
import ca.efriesen.lydia.controllers.ButtonControllers.*;
import ca.efriesen.lydia.databases.ButtonConfigDataSource;

import java.util.*;

/**
 * Created by eric on 2014-06-14.
 */
public class ButtonController implements View.OnClickListener, View.OnLongClickListener {

	private final static String TAG = "Lydia button controller";
	private Activity activity;
	public HomeScreenActivityCallback homeScreenActivityCallback;

	// create a new hashmap that takes the action and maps it to a class
	private Map<String, MyButton> buttons = new HashMap<String, MyButton>();
	private boolean admin = false;

	// request code for edit activity
	public static final int EDIT_BUTTON = 1;

	public ButtonController(Activity activity) {
		this.activity = activity;
		// add all the possible actions and classes
		buttons.put(AirRideButton.ACTION, new AirRideButton(activity));
		buttons.put(AndroidButton.ACTION, new AndroidButton(activity));
		buttons.put(ChromeButton.ACTION, new ChromeButton(activity));
		buttons.put(MusicButton.ACTION, new MusicButton(activity));
		buttons.put(NavigationButton.ACTION, new NavigationButton(activity));
		buttons.put(PhoneButton.ACTION, new PhoneButton(activity));
		buttons.put(SettingsButton.ACTION, new SettingsButton(activity));
	}

	public ArrayList<MyButton> getButtons() {
		ArrayList<MyButton> list = new ArrayList<MyButton>(buttons.values());

		// sort alphabetically
		Collections.sort(list, new Comparator<MyButton>() {
			@Override
			public int compare(MyButton o, MyButton o2) {
				return o.getAction().compareToIgnoreCase(o2.getAction());
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
				MyButton button = buttons.get(passedButton.getAction());
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
				MyButton button = buttons.get(passedButton.getAction());
				button.onLongClick();
			// admin mode, remove the button
			} else {
				removeButton(passedButton);
			}
		} catch (Exception e) {}
		// we've handled the click.
		return true;
	}

	public boolean hasValidSettingsButton() {
		ButtonConfigDataSource dataSource = new ButtonConfigDataSource(activity);
		dataSource.open();
		boolean hasButton = dataSource.hasSettingsButton();
		dataSource.close();
		return hasButton;
	}

	public void cleanup() {
		for (Map.Entry<String , MyButton>entry : buttons.entrySet()) {
			MyButton button = entry.getValue();
			button.cleanUp();
		}
	}

	public void clearButtons() {
		for (int i=0; i<6; i++) {
			int resId = activity.getResources().getIdentifier("settings_home" + i, "id", activity.getPackageName());
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
		int numButtons = 6;
		String idName = "home";
		if (admin) {
			idName = "settings_home";
		}

		if (admin) {
			// set defaults, override them after
			for (int i = 0; i < numButtons; i++) {
				int resId = activity.getResources().getIdentifier(idName + i, "id", activity.getPackageName());
				// get the button
				Button button = (Button) activity.findViewById(resId);
				ca.efriesen.lydia.databases.Button myButton = new ca.efriesen.lydia.databases.Button();
				myButton.setDisplayArea(1);
				myButton.setPosition(i);
				button.setTag(myButton);
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
	private void removeButton(ca.efriesen.lydia.databases.Button button) {
		// open the db
		ButtonConfigDataSource dataSource = new ButtonConfigDataSource(activity);
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
			ButtonConfigDataSource dataSource = new ButtonConfigDataSource(activity);
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
}
