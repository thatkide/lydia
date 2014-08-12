package ca.efriesen.lydia.activities.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.RFIDTag;
import ca.efriesen.lydia.databases.RFIDTagDataSource;
import ca.efriesen.lydia_common.includes.Intents;

/**
 * Created by eric on 2013-08-18.
 */
public class RFIDTagConfig extends Activity {
	private static final String TAG = "lydia rfid config";
	private RFIDTagDataSource dataSource;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rfid_tag_config);

		final Bundle extras = getIntent().getExtras();
		if (extras != null) {
			dataSource = new RFIDTagDataSource(this);
			dataSource.open();
			final RFIDTag tag = (RFIDTag) extras.getSerializable("rfid_tag");

			((TextView)findViewById(R.id.tag_description_title)).setText(tag.getDescription());
			final CheckBox enabled = (CheckBox)findViewById(R.id.tag_enabled);
			enabled.setChecked(tag.getEnabled());

			final CheckBox startCar = (CheckBox)findViewById(R.id.start_car);
			startCar.setChecked(tag.getStartCar());

			final CheckBox unlockDoors = (CheckBox)findViewById(R.id.unlock_doors);
			unlockDoors.setChecked(tag.getUnlockDoors());

			final EditText description = (EditText)findViewById(R.id.tag_description);
			description.setText(tag.getDescription());

			Button save = (Button) findViewById(R.id.save_tag);
			save.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					// update the fields
					tag.setDescription(description.getText().toString());
					tag.setEnabled(enabled.isChecked());
					tag.setUnlockDoors(unlockDoors.isChecked());
					tag.setStartCar(startCar.isChecked());
					dataSource.update(tag);

					// create a new intent
					Intent tagIntent = new Intent(Intents.RFID);
					// put the tag as an extra
					tagIntent.putExtra("rfid_tag", tag);
					// also add the list id position
					tagIntent.putExtra("list_id", extras.getInt("list_id"));
					// set the result and close
					setResult(RESULT_OK, tagIntent);
					finish();
				}
			});

			((TextView)findViewById(R.id.tag_number)).setText(String.valueOf(tag.getTagNumber()));

		}
	}
}