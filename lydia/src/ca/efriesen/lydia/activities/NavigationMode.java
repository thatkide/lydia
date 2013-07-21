package ca.efriesen.lydia.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.includes.GMapV2Direction;

import java.util.ArrayList;

/**
 * Created by eric on 2013-07-21.
 */
public class NavigationMode extends Activity implements AdapterView.OnItemClickListener {
	private static final String TAG = "lydia navigation mode";


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutInflater().inflate(R.layout.navigation_mode, null));

		ArrayList<String> modes = GMapV2Direction.getModes();
		ListView listView = (ListView) findViewById(R.id.navigation_modes);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, modes);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		Intent intent = new Intent();
		intent.putExtra("mode", position);
		setResult(RESULT_OK, intent);
		finish();
	}
}
