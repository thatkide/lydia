package com.autosenseapp.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.autosenseapp.R;
import com.appaholics.updatechecker.UpdateChecker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by eric on 1/16/2014.
 */
public class UpdateActivity extends Activity implements Button.OnClickListener{

	private UpdateChecker checker;
	private SharedPreferences sharedPreferences;

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.update_check_activity);

		sharedPreferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);

		Button check = (Button) findViewById(R.id.check_for_update);
		check.setOnClickListener(this);

		checker = new UpdateChecker(this, true);
		checker.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object o) {
				if (checker.isUpdateAvailable()) {
					checker.downloadAndInstall(getString(R.string.update_apk_url));
				} else {
					Toast.makeText(getApplicationContext(), "Already up to date", Toast.LENGTH_SHORT).show();
				}
			}
		});

		Long lastUpdateCheck = sharedPreferences.getLong("lastUpdateCheck", 0);
		setLastCheckText(lastUpdateCheck);

	}

	private void setLastCheckText(Long time) {
		Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
		calendar.setTimeInMillis(time);
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		TextView updateCheck = (TextView) findViewById(R.id.last_update_check);

		updateCheck.setText("Last checked: " + format.format(calendar.getTime()));
	}

	@Override
	public void onClick(View view) {
		long currentTime = System.currentTimeMillis();
		sharedPreferences.edit()
				.putLong("lastUpdateCheck", currentTime)
				.apply();
		setLastCheckText(currentTime);

		checker.checkForUpdateByVersionCode(getString(R.string.update_url));
	}
}
