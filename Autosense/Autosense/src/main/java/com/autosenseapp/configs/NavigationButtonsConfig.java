package com.autosenseapp.configs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import com.autosenseapp.R;
import com.autosenseapp.buttons.BaseButton;
import com.autosenseapp.buttons.navButtons.ClearMapButton;
import com.autosenseapp.buttons.navButtons.MapDirectionsMode;
import com.autosenseapp.buttons.navButtons.ToggleTrafficButton;
import com.autosenseapp.callbacks.ButtonCheckerCallback;
import com.autosenseapp.databases.Button;

/**
 * Created by eric on 2014-07-06.
 */
public class NavigationButtonsConfig implements ButtonCheckerCallback {

	private int group = BaseButton.GROUP_NAVIGATION;
	private int type = BaseButton.TYPE_SIDEBAR_LEFT;

	@Override
	public List<Button> getButtons(Activity activity) {
		List<Button> buttons = new ArrayList<Button>();
		int area = 0;

		Button mapDirections = new Button();
		mapDirections.setAction(MapDirectionsMode.class.getSimpleName());
		mapDirections.setButtonType(type);
		mapDirections.setDisplayArea(area);
		mapDirections.setGroup(group);
		mapDirections.setPosition(0);
		mapDirections.setTitle("Driving");
		mapDirections.setUsesDrawable(false);
		mapDirections.setDrawable("blank");

		Button toggleTraffic = new Button();
		toggleTraffic.setAction(ToggleTrafficButton.class.getSimpleName());
		toggleTraffic.setButtonType(type);
		toggleTraffic.setDisplayArea(area);
		toggleTraffic.setGroup(group);
		toggleTraffic.setPosition(1);
		toggleTraffic.setTitle(activity.getString(R.string.toggle_traffic));
		toggleTraffic.setUsesDrawable(false);
		toggleTraffic.setDrawable("blank");

		Button clearMap = new Button();
		clearMap.setAction(ClearMapButton.class.getSimpleName());
		clearMap.setButtonType(type);
		clearMap.setDisplayArea(area);
		clearMap.setGroup(group);
		clearMap.setPosition(2);
		clearMap.setTitle(activity.getString(R.string.clear));
		clearMap.setUsesDrawable(false);
		clearMap.setDrawable("blank");

		SharedPreferences sharedPreferences = activity.getSharedPreferences(activity.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		// modes
		// 0 - biking
		// 1 - driving
		// 2 - walking
		// 3 - transit
		sharedPreferences.edit().putInt("nav_mode", 1).apply();

		buttons.add(mapDirections);
		buttons.add(toggleTraffic);
		buttons.add(clearMap);

		return buttons;
	}

	@Override
	public int getGroup() {
		return group;
	}

	@Override
	public int getType() {
		return type;
	}

}
