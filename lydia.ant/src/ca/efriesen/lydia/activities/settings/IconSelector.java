package ca.efriesen.lydia.activities.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.ButtonConfigOpenHelper;
import ca.efriesen.lydia.includes.ImageAdapter;

/**
 * Created by eric on 2014-06-15.
 */
public class IconSelector extends Activity {

	private static final String TAG = "icon selector";

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.icon_selector);

		String drawables[] = getResources().getStringArray(R.array.homescreen_images);

		GridView view = (GridView) findViewById(R.id.icon_gridview);
		view.setAdapter(new ImageAdapter(this, drawables));

		view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				// the name is stored in the tag of the view pressed.  so view.getTag() returnes the file name
				Intent returnIntent = new Intent();
				returnIntent.putExtra(ButtonConfigOpenHelper.DRAWABLE, (String)view.getTag());
				setResult(RESULT_OK, returnIntent);
				finish();
			}
		});
	}
}
