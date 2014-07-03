package ca.efriesen.lydia.activities.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.controllers.ButtonController;
import ca.efriesen.lydia.buttons.BaseButton;

import java.util.List;

/**
 * Created by eric on 2014-06-21.
 */
public class SidebarEditorActivity extends Activity implements DrawScreenCallback {

	private static final String TAG = "sidebar editor";

	private ButtonController driverButtonController;
	private ButtonController passengerButtonController;

	public static final int numButtons = 3;
	public static final String DRIVERBASENAME = "driver_";
	public static final String PASSENGERBASENAME = "passenger_";

	private ImageButton driverUp;
	private ImageButton driverDown;
	private LinearLayout driverAdminNavGroup;

	private ImageButton passengerUp;
	private ImageButton passengerDown;
	private LinearLayout passengerAdminNavGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.sidebar_editor);

		driverButtonController = new ButtonController(this, DRIVERBASENAME, BaseButton.TYPE_SIDEBAR_LEFT, numButtons, true);
		passengerButtonController = new ButtonController(this, PASSENGERBASENAME, BaseButton.TYPE_SIDEBAR_RIGHT, numButtons, true);

		Button driverAdd = (Button) findViewById(R.id.driver_add);
		Button driverDelete = (Button) findViewById(R.id.driver_delete);

		Button passengerAdd = (Button) findViewById(R.id.passenger_add);
		Button passengerDelete = (Button) findViewById(R.id.passenger_delete);

		driverUp = (ImageButton) findViewById(R.id.driver_up);
		driverDown = (ImageButton) findViewById(R.id.driver_down);
		ImageButton driverUp2 = (ImageButton) findViewById(R.id.driver_up_2);
		ImageButton driverDown2 = (ImageButton) findViewById(R.id.driver_down_2);
		driverAdminNavGroup = (LinearLayout) findViewById(R.id.driver_nav_group);

		passengerUp = (ImageButton) findViewById(R.id.passenger_up);
		passengerDown = (ImageButton) findViewById(R.id.passenger_down);
		ImageButton passengerUp2 = (ImageButton) findViewById(R.id.passenger_up_2);
		ImageButton passengerDown2 = (ImageButton) findViewById(R.id.passenger_down_2);
		passengerAdminNavGroup = (LinearLayout) findViewById(R.id.passenger_nav_group);

		driverAdd.setTag(BaseButton.BUTTON_SCREEN_ADD);
		driverAdd.setOnClickListener(driverButtonController);
		driverDelete.setTag(BaseButton.BUTTON_SCREEN_DELETE);
		driverDelete.setOnClickListener(driverButtonController);

		passengerAdd.setTag(BaseButton.BUTTON_SCREEN_ADD);
		passengerAdd.setOnClickListener(passengerButtonController);
		passengerDelete.setTag(BaseButton.BUTTON_SCREEN_DELETE);
		passengerDelete.setOnClickListener(passengerButtonController);

		// set tag so the controller knows what the button is for
		driverUp.setTag(BaseButton.BUTTON_NEXT);
		driverUp.setOnClickListener(driverButtonController);
		driverUp2.setTag(BaseButton.BUTTON_NEXT);
		driverUp2.setOnClickListener(driverButtonController);
		driverDown.setTag(BaseButton.BUTTON_PREV);
		driverDown.setOnClickListener(driverButtonController);
		driverDown2.setTag(BaseButton.BUTTON_PREV);
		driverDown2.setOnClickListener(driverButtonController);

		passengerUp.setTag(BaseButton.BUTTON_NEXT);
		passengerUp.setOnClickListener(passengerButtonController);
		passengerUp2.setTag(BaseButton.BUTTON_NEXT);
		passengerUp2.setOnClickListener(passengerButtonController);
		passengerDown.setTag(BaseButton.BUTTON_PREV);
		passengerDown.setOnClickListener(passengerButtonController);
		passengerDown2.setTag(BaseButton.BUTTON_PREV);
		passengerDown2.setOnClickListener(passengerButtonController);

		for (int i=0; i<numButtons; i++) {
			// get the resource id for the button
			int driverResId = getResources().getIdentifier(DRIVERBASENAME + i, "id", getPackageName());
			int passengerResId = getResources().getIdentifier(PASSENGERBASENAME + i, "id", getPackageName());
			// get the button
			Button driverButton = (Button) findViewById(driverResId);
			driverButton.setOnClickListener(driverButtonController);
			driverButton.setOnLongClickListener(driverButtonController);

			Button passengerButton = (Button) findViewById(passengerResId);
			passengerButton.setOnClickListener(passengerButtonController);
			passengerButton.setOnLongClickListener(passengerButtonController);
		}

		driverButtonController.drawScreen();
		passengerButtonController.drawScreen();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		driverButtonController.onActivityResult(requestCode, resultCode, data);
		passengerButtonController.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void drawScreen(List<ca.efriesen.lydia.databases.Button> buttons) {
		int numDriverScrens = driverButtonController.getNumScreens();
		int selectedDriverScreen = driverButtonController.getSelectedScreen();

		if (numDriverScrens == 1) {
			driverUp.setEnabled(false);
		} else {
			driverUp.setEnabled(true);
		}
		if (numDriverScrens > 1 && selectedDriverScreen == (numDriverScrens -1)) {
			driverDown.setVisibility(View.VISIBLE);
			driverUp.setVisibility(View.GONE);
			driverAdminNavGroup.setVisibility(View.GONE);
		} else if (numDriverScrens > 2 && (selectedDriverScreen != 0) && (selectedDriverScreen != numDriverScrens-1) ) {
			driverAdminNavGroup.setVisibility(View.VISIBLE);
			driverDown.setVisibility(View.GONE);
			driverUp.setVisibility(View.GONE);
		}

		int numPassengerScreens = passengerButtonController.getNumScreens();
		int selectedPassengerScreen = passengerButtonController.getSelectedScreen();

		if (numPassengerScreens == 1) {
			passengerUp.setEnabled(false);
		} else {
			passengerUp.setEnabled(true);
		}
		if (numPassengerScreens > 1 && selectedPassengerScreen == (numPassengerScreens -1)) {
			passengerDown.setVisibility(View.VISIBLE);
			passengerUp.setVisibility(View.GONE);
			passengerAdminNavGroup.setVisibility(View.GONE);
		} else if (numPassengerScreens > 2 && (selectedPassengerScreen != 0) && (selectedPassengerScreen != numPassengerScreens-1) ) {
			passengerAdminNavGroup.setVisibility(View.VISIBLE);
			passengerDown.setVisibility(View.GONE);
			passengerUp.setVisibility(View.GONE);
		}

	}
}