package com.autosenseapp;

import android.app.Application;
import com.autosenseapp.controllers.BackgroundController;
import com.autosenseapp.controllers.Controller;
import com.autosenseapp.controllers.NotificationController;
import com.autosenseapp.controllers.PinTriggerController;

/**
 * Created by eric on 2014-09-02.
 */
public class GlobalClass extends Application {

	public static final int BACKGROUND_CONTROLLER = 2;
	public static final int NOTIFICATION_CONTROLLER = 1;
	public static final int PIN_TRIGGER_CONTROLLER = 3;

	private BackgroundController backgroundController;
	private NotificationController notificationController;
	private PinTriggerController pinTriggerController;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		backgroundController.onDestroy();
		notificationController.onDestroy();
		pinTriggerController.onDestroy();
	}

	public void setController(int type, Controller controller) {
		switch (type) {
			case BACKGROUND_CONTROLLER: {
				this.backgroundController = (BackgroundController) controller;
				break;
			}
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
			case BACKGROUND_CONTROLLER:
				return backgroundController;
			case NOTIFICATION_CONTROLLER:
				return notificationController;
			case PIN_TRIGGER_CONTROLLER:
				return pinTriggerController;
		}
		return null;
	}

}
