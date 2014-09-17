package com.autosenseapp;

import android.app.Application;
import com.autosenseapp.controllers.Controller;
import com.autosenseapp.controllers.NotificationController;
import com.autosenseapp.controllers.PinTriggerController;
import java.util.Arrays;
import java.util.List;
import dagger.ObjectGraph;

/**
 * Created by eric on 2014-09-02.
 */
public class AutosenseApplication extends Application {

	private ObjectGraph applicationGraph;

	public static final int NOTIFICATION_CONTROLLER = 1;
	public static final int PIN_TRIGGER_CONTROLLER = 3;

	private NotificationController notificationController;
	private PinTriggerController pinTriggerController;

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
		notificationController.onDestroy();
		pinTriggerController.onDestroy();
	}

	public void setController(int type, Controller controller) {
		switch (type) {
			case NOTIFICATION_CONTROLLER: {
				this.notificationController = (NotificationController) controller;
				break;
			}
			case PIN_TRIGGER_CONTROLLER: {
				this.pinTriggerController = (PinTriggerController) controller;
				break;
			}
		}
	}

	public Controller getController(int controller) {
		switch (controller) {
			case NOTIFICATION_CONTROLLER:
				return notificationController;
			case PIN_TRIGGER_CONTROLLER:
				return pinTriggerController;
		}
		return null;
	}
}
