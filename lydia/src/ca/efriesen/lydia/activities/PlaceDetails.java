package ca.efriesen.lydia.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia_common.includes.Intents;

/**
 * Created by eric on 2013-07-14.
 */
public class PlaceDetails extends Activity {

	private static final String TAG = "lydia place details";
	private Address address;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutInflater().inflate(R.layout.place_details, null));

		try {
			Intent intent = getIntent();
			address = intent.getParcelableExtra("address");

			TextView name = (TextView) findViewById(R.id.place_name);
			TextView addy = (TextView) findViewById(R.id.place_address);
			TextView phone = (TextView) findViewById(R.id.place_phone_number);

			TextView directionsText = (TextView) findViewById(R.id.place_directions_text);
			ImageView directionsImage = (ImageView) findViewById(R.id.place_directions_image);

			directionsText.setOnClickListener(directionsClick);
			directionsImage.setOnClickListener(directionsClick);

			name.setText(address.getFeatureName());
			addy.setText(address.getAddressLine(0));
			phone.setText(address.getPhone());

		} catch (Exception e) {
			Log.e(TAG, "Intent error", e);
		}
	}


	private View.OnClickListener directionsClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			sendBroadcast(new Intent(Intents.GETDIRECTIONS).putExtra("address", address));
			finish();
		}
	};
}