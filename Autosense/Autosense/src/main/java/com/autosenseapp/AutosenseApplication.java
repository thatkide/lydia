package com.autosenseapp;

import android.app.Application;
import java.util.Arrays;
import java.util.List;
import dagger.ObjectGraph;

/**
 * Created by eric on 2014-09-02.
 */
public class AutosenseApplication extends Application {

	private ObjectGraph applicationGraph;

	@Override
	public void onCreate() {
		super.onCreate();
		applicationGraph = ObjectGraph.create(getModules().toArray());
	}

	protected List<Object> getModules() {
		return Arrays.<Object>asList(
				new AndroidModule(this)
		);
	}

	public void inject(Object object) {
		applicationGraph.inject(object);
	}

	ObjectGraph getApplicationGraph() {
		return applicationGraph;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}
