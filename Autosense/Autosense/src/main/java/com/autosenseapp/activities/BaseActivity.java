package com.autosenseapp.activities;

import android.app.Activity;
import android.os.Bundle;
import com.autosenseapp.AutosenseApplication;

/**
 * Created by eric on 2014-09-16.
 */
public abstract class BaseActivity extends Activity {

	@Override protected void onCreate(Bundle saved) {
		super.onCreate(saved);

		((AutosenseApplication)getApplication()).inject(this);
	}
}
