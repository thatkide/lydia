package ca.efriesen.lydia.activities.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.controllers.ButtonController;
import ca.efriesen.lydia.databases.ButtonConfigDataSource;
import java.util.List;

/**
 * Created by eric on 2014-06-14.
 */
public class HomeScreenEditorActivity extends Activity {

	private static final String TAG = "homescreen editor";

	private ButtonController buttonController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.home_screen_layout);

		buttonController = new ButtonController(this);
		buttonController.setAdmin(true);
		ButtonConfigDataSource dataSource = new ButtonConfigDataSource(this);
		dataSource.open();
		// get the buttons in our area
		List<ca.efriesen.lydia.databases.Button> buttons = dataSource.getButtonsInArea(1);
		// close the db, we don't need it any more
		dataSource.close();

		int numButtons = 6;

		// tell every button to call the button controller, it will decide your fate
		for (int i=0; i<numButtons; i++) {
			// get the resource id for the button
			int resId = getResources().getIdentifier("settings_home" + i, "id", getPackageName());
			// get the button
			Button button = (Button) findViewById(resId);
			button.setOnClickListener(buttonController);
			button.setOnLongClickListener(buttonController);
		}

		// this draws the buttons that are actually populated
		buttonController.populateButton(buttons);

		// provide a callback to refresh the buttons once the long click has finished deleting the button from the db
		buttonController.homeScreenActivityCallback = new HomeScreenActivityCallback() {
			@Override
			public void onLayoutChange(List<ca.efriesen.lydia.databases.Button> buttons) {
				// refresh the onscreen display
				buttonController.clearButtons();
				buttonController.populateButton(buttons);
			}
		};
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		buttonController.onActivityResult(requestCode, resultCode, data);
	}
}
