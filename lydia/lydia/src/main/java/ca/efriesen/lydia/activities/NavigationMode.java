package ca.efriesen.lydia.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.directions.route.Routing;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.Button;
import ca.efriesen.lydia.databases.ButtonConfigDataSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by eric on 2013-07-21.
 */
public class NavigationMode extends Activity implements AdapterView.OnItemClickListener {
	private static final String TAG = NavigationMode.class.getSimpleName();

	private List<Routing.TravelMode> modes;
	private Button button;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutInflater().inflate(R.layout.navigation_mode, null));

		Bundle extras = getIntent().getExtras();
		button = (Button) extras.get("button");

		modes = new ArrayList<Routing.TravelMode>(Arrays.asList(Routing.TravelMode.values()));

//		// sort the list alphabetically
//		Collections.sort(modes, new Comparator<Routing.TravelMode>() {
//			@Override
//			public int compare(Routing.TravelMode lhs, Routing.TravelMode rhs) {
//				return lhs.toString().compareToIgnoreCase(rhs.toString());
//			}
//		});

		ListView listView = (ListView) findViewById(R.id.navigation_modes);
		ArrayAdapter<Routing.TravelMode> adapter = new ArrayAdapter<Routing.TravelMode>(this, android.R.layout.simple_list_item_1, modes);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		// edit the button text
		String title = modes.get(position).toString().toLowerCase();
		title = Character.toUpperCase(title.charAt(0)) + title.substring(1);
		button.setTitle(title);

		// store it in shared prefs
		SharedPreferences sharedPreferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		sharedPreferences.edit().putInt("nav_mode", position).apply();

		ButtonConfigDataSource dataSource = new ButtonConfigDataSource(this);
		dataSource.open();
		dataSource.editButton(button);
		dataSource.close();


		Intent intent = new Intent();
		intent.putExtra("mode", modes.get(position));
		setResult(RESULT_OK, intent);
		finish();
	}
}
