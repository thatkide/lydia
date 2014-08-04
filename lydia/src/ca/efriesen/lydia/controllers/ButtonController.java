package ca.efriesen.lydia.controllers;

import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.settings.ButtonEditor;
import ca.efriesen.lydia.buttons.appButtons.AppLaunchButton;
import ca.efriesen.lydia.callbacks.DrawScreenCallback;
import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.databases.ButtonConfigDataSource;

import java.util.*;

/**
 * Created by eric on 2014-06-14.
 */
public class ButtonController implements View.OnClickListener, View.OnLongClickListener, View.OnDragListener {

	private final static String TAG = ButtonController.class.getSimpleName();
	private Activity activity;
	private Fragment fragment;
	private ButtonConfigDataSource dataSource;

	// create a new hashmap that takes the action and maps it to a class
	private Map<String, BaseButton> buttons = new HashMap<String, BaseButton>();
	private boolean admin = false;
	private int group;
	final private String baseName;
	final private int buttonType;
	private int adminNumButtons;
	private int numScreens;
	private int selectedScreen = 0;

	private LinearLayout buttonDeleteZone;
	private ImageView buttonDeleteImage;
	private TextView buttonDeleteText;

	// request code for edit activity
	public static final int EDIT_BUTTON = 1;

	public ButtonController(Activity activity, final String baseName, final int buttonType) {
		this(activity, baseName, buttonType, 0, false, BaseButton.GROUP_USER);
	}

	public ButtonController(Fragment fragment, final String baseName, final int buttonType, int group) {
		this(fragment.getActivity(), baseName, buttonType, 0, false, group);
		this.fragment = fragment;
	}

	public ButtonController(Activity activity, final String baseName, final int buttonType, final int adminNumButtons, boolean adminMode, int group) {
		this.activity = activity;
		this.baseName = baseName;
		this.admin = adminMode;
		this.buttonType = buttonType;
		this.adminNumButtons = adminNumButtons;
		this.group = group;

		dataSource = new ButtonConfigDataSource(activity);

		dataSource.open();
		numScreens = dataSource.numScreensPerGroup(buttonType, group);
		dataSource.close();

		// get all possible buttons from the base button class
		buttons = BaseButton.getAllButtons(activity);

		// Delete zone is enabled twice.  Once for driver side and again for passenger.
		// Passenger overwrites the driver and when the driver side is delete, it won't refresh because it has the passenger instance
		// I've fixed this by implementing a callback to let the calling activity decide what to refresh
		if (adminMode) {
			buttonDeleteZone = (LinearLayout) activity.findViewById(R.id.button_delete_zone);
			buttonDeleteImage = (ImageView) activity.findViewById(R.id.button_delete_image);
			buttonDeleteText = (TextView) activity.findViewById(R.id.button_delete_text);
			buttonDeleteZone.setOnDragListener(this);
		}
	}

	// ------------------------------ Admin Methods ------------------------------ //

	// Must be called from activity
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// after we've edited a button, redraw the screen
		if (requestCode == EDIT_BUTTON && resultCode == Activity.RESULT_OK) {
			drawScreen();
		}
	}

	// clears all images and text from the buttons and removes the button stored within
	public void clearButtons() {
		for (int i=0; i< adminNumButtons; i++) {
			int resId = activity.getResources().getIdentifier(baseName + i, "id", activity.getPackageName());
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

	public ArrayList<BaseButton> getButtonActions() {
		// get a map of all available user buttons
		Map<String, BaseButton> buttons = BaseButton.getUserButtons(activity);
		// translate it into an array list
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

	@Override
	public boolean onDrag(View view, DragEvent dragEvent) {
		// this is the original button
		View v = (View) dragEvent.getLocalState();
		// this is the button that was dragged
		ca.efriesen.lydia.databases.Button button = (ca.efriesen.lydia.databases.Button) v.getTag();

		// rearrange
		if (view.getTag() instanceof ca.efriesen.lydia.databases.Button) {
			switch (dragEvent.getAction()) {
				case DragEvent.ACTION_DRAG_ENTERED: {
					// get the proper background resource
					int pos = ((ca.efriesen.lydia.databases.Button) view.getTag()).getPosition();
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
					// get the position it was dropped
					int pos = ((ca.efriesen.lydia.databases.Button)view.getTag()).getPosition();
					// set the original background back
					view.setBackground(getButtonBgFromPos(pos));
					// open the datasouce, we'll switch buttons, and get the updated list in one go
					dataSource.open();
					// but this button in this position, switching the two
					dataSource.switchButtons(button, pos);
					// close the db, we don't need it any more
					dataSource.close();

					// this draws the buttons that are actually populated
					drawScreen();

					break;
				}
			}

			// remove
		} else {
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
					dataSource.open();
					dataSource.removeButton(button);
					dataSource.close();
					// check if the activity has the callback
					if (activity instanceof DrawScreenCallback) {
						// if so, check if it wants to redraw.
						if (!((DrawScreenCallback)activity).fullDraw()) {
							// no? ok, we'll do it
							drawScreen();
						}
					}

					// we might also have been passed a fragment.  if so check it too
					if (fragment instanceof DrawScreenCallback) {
						// if so, check if it wants to redraw.
						if (!((DrawScreenCallback)fragment).fullDraw()) {
							// no? ok, we'll do it
							drawScreen();
						}
					}
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
		}
		return true;
	}

	// get the proper rounded corners from where the button is located
	private Drawable getButtonBgFromPos(int pos) {
		Drawable background;
		if (buttonType == BaseButton.TYPE_HOMESCREEN) {
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
		} else {
			background = activity.getResources().getDrawable(R.drawable.button_bg);
		}
		return background;
	}


	// ------------------------------ Global Methods ------------------------------ //

	public void cleanup() {
		for (Map.Entry<String , BaseButton>entry : buttons.entrySet()) {
			try {
				BaseButton button = entry.getValue();
				button.onStop();
			} catch (NullPointerException e) { }
		}
	}

	public void drawScreen(int selectedScreen) {
		this.selectedScreen = selectedScreen;
		drawScreen();
	}

	// gets all the buttons
	public void drawScreen() {
		clearButtons();
		dataSource.open();
		// get the buttons in our area
		List<ca.efriesen.lydia.databases.Button> buttonsInDb = dataSource.getButtonsInArea(buttonType, selectedScreen, group);

		// close the db, we don't need it any more
		dataSource.close();

		// check if the activity has the callback
		if (activity instanceof DrawScreenCallback) {
			((DrawScreenCallback) activity).drawScreen(buttonsInDb);
		}

		// we might also have been passed a fragment.  if so check it too
		if (fragment instanceof DrawScreenCallback) {
			((DrawScreenCallback) fragment).drawScreen(buttonsInDb);
		}

		if (admin) {
			// set defaults, override them after
			for (int i = 0; i < adminNumButtons; i++) {
				int resId = activity.getResources().getIdentifier(baseName + i, "id", activity.getPackageName());
				// get the button
				Button button = (Button) activity.findViewById(resId);
				ca.efriesen.lydia.databases.Button myButton = new ca.efriesen.lydia.databases.Button();
				myButton.setDisplayArea(selectedScreen);
				myButton.setPosition(i);
				button.setTag(myButton);
				button.setOnDragListener(this);
			}
		}

		// loop over all buttons and populate accordingly
		for (int i=0; i<buttonsInDb.size(); i++) {
			// get the button
			ca.efriesen.lydia.databases.Button myButton = buttonsInDb.get(i);
			// run the onstart method of the base button
			// get the resource id for the button
			int resId = activity.getResources().getIdentifier(baseName + myButton.getPosition(), "id", activity.getPackageName());
			// set the res id for the buttons
			(buttons.get(myButton.getAction())).setResourceName(baseName + myButton.getPosition());
			// this must be after the setresource command above
			BaseButton baseButton = buttons.get(myButton.getAction());
			baseButton.onStart();
			// get the button
			Button button = (Button) activity.findViewById(resId);
			// set the text to the proper title
			try {
				String title = myButton.getTitle();
				if (title.length() > 12 ) {
					// if the length of the title is too long, truncate it
					title = title.substring(0, 12);
				}
				button.setText(title);
			} catch (Exception e) {}
			// try the background image if set
			if (myButton.getUsesDrawable()) {
				Drawable img;
				if (!myButton.getDrawable().equalsIgnoreCase("blank")) {
					// get the image resource id
					int imgId = activity.getResources().getIdentifier(myButton.getDrawable(), "drawable", activity.getPackageName());
					// get the drawable
					img = activity.getResources().getDrawable(imgId);
				} else {
					if (baseButton instanceof AppLaunchButton) {
						Drawable drawable = ((AppLaunchButton)baseButton).getIcon(myButton.getExtraData());
						Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
						img = new BitmapDrawable(activity.getResources(), Bitmap.createScaledBitmap(bitmap, 100, 100, true));
					} else {
						img = activity.getResources().getDrawable(R.drawable.blank);
					}
				}
				// set it to the top on the button
				button.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
			}
			button.setTag(myButton);
		}
	}

	public boolean hasValidSettingsButton(int buttonType) {
		dataSource.open();
		boolean hasButton = dataSource.hasSettingsButton(buttonType);
		dataSource.close();
		return hasButton;
	}

	public int getNumScreens() {
		return numScreens;
	}

	public int getSelectedScreen() {
		return selectedScreen;
	}

	@Override
	public void onClick(View view) {
		int buttonAction = 0;
		if (view.getTag() instanceof  Integer) {
			buttonAction = (Integer) view.getTag();
		}

		try {
			// non admin mode, do what's defined in the button class
			if (!admin) {
				if (view.getTag() instanceof ca.efriesen.lydia.databases.Button) {
					// get the button pressed
					ca.efriesen.lydia.databases.Button passedButton = (ca.efriesen.lydia.databases.Button) view.getTag();
					// get the button from the hashmap, and execute the onclick method
					BaseButton button = buttons.get(passedButton.getAction());
					button.onClick(view, passedButton);
				} else if (view.getTag() instanceof Integer) {
					switch (buttonAction) {
						case BaseButton.BUTTON_NEXT: {
							if (selectedScreen < numScreens - 1) {
								selectedScreen++;
							} else {
								selectedScreen = 0;
							}
							break;
						}
						case BaseButton.BUTTON_PREV: {
							if (selectedScreen > 0) {
								selectedScreen--;
							} else {
								selectedScreen = numScreens - 1;
							}
							break;
						}
					}
				}
			// admin mode, edit the button
			} else {
				switch (buttonAction) {
					case BaseButton.BUTTON_NEXT: {
						if (selectedScreen < numScreens-1) {
							selectedScreen++;
						} else {
							selectedScreen = 0;
						}
						drawScreen();
						break;
					}
					case BaseButton.BUTTON_PREV: {
						if (selectedScreen > 0) {
							selectedScreen--;
						} else {
							selectedScreen = numScreens-1;
						}
						drawScreen();
						break;
					}
					case BaseButton.BUTTON_SCREEN_ADD: {
						Log.d(TAG, "num screens is " + numScreens);
						numScreens++;
						Log.d(TAG, "num screens is now " + numScreens);
						drawScreen();
						break;
					}
					case BaseButton.BUTTON_SCREEN_DELETE: {
						numScreens--;
						if (numScreens > 1) {
							// remove the screen from the db and update the others
							dataSource = new ButtonConfigDataSource(activity);
							dataSource.open();
							dataSource.removeScreen(buttonType, selectedScreen, numScreens);
							// close the db, we don't need it any more
							dataSource.close();
						}
						// if we've deleted the last screen, set selected to one less
						if (selectedScreen >= numScreens) {
							selectedScreen = numScreens - 1;
						}
						drawScreen();
						break;
					}
					default: {
						// get the button pressed
						ca.efriesen.lydia.databases.Button passedButton = (ca.efriesen.lydia.databases.Button) view.getTag();
						// start the edit button activity.  it will return a result, which will be passed back here to onActivityResult, which will update the UI
						Intent buttonEditor = new Intent(activity, ButtonEditor.class);
						buttonEditor.putExtra("button", passedButton);
						buttonEditor.putExtra("buttonType", buttonType);

						activity.startActivityForResult(buttonEditor, EDIT_BUTTON);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// call the activity onclick listener, let it do its thing
		if (activity instanceof View.OnClickListener) {
			((View.OnClickListener) activity).onClick(view);
		}
		if (fragment instanceof View.OnClickListener) {
			((View.OnClickListener) fragment).onClick(view);
		}
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
			} else {
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
}