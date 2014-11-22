package com.autosenseapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.autosenseapp.AutosenseApplication;

/**
 * Created by eric on 2014-09-22.
 */
public abstract class BaseService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		((AutosenseApplication)getApplication()).inject(this);
	}
}
