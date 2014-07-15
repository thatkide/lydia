package ca.efriesen.lydia.configs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.buttons.appButtons.NavigationButton;
import ca.efriesen.lydia.buttons.navButtons.ClearMapButton;
import ca.efriesen.lydia.buttons.navButtons.MapDirectionsMode;
import ca.efriesen.lydia.buttons.navButtons.NavSearchButton;
import ca.efriesen.lydia.buttons.navButtons.ToggleTrafficButton;
import ca.efriesen.lydia.callbacks.ButtonCheckerCallback;
import ca.efriesen.lydia.databases.Button;

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

		Button navSearch = new Button();
		navSearch.setAction(NavSearchButton.class.getSimpleName());
		navSearch.setButtonType(type);
		navSearch.setDisplayArea(area);
		navSearch.setGroup(group);
		navSearch.setPosition(0);
		navSearch.setTitle(activity.getString(R.string.search));
		navSearch.setUsesDrawable(false);
		navSearch.setDrawable("blank");

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

		Button mapDirections = new Button();
		mapDirections.setAction(MapDirectionsMode.class.getSimpleName());
		mapDirections.setButtonType(type);
		mapDirections.setDisplayArea(area+1);
		mapDirections.setGroup(group);
		mapDirections.setPosition(0);
		mapDirections.setTitle("Driving");
		mapDirections.setUsesDrawable(false);
		mapDirections.setDrawable("blank");

		SharedPreferences sharedPreferences = activity.getSharedPreferences(activity.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
		// modes
		// 0 - biking
		// 1 - driving
		// 2 - walking
		// 3 - transit
		sharedPreferences.edit().putInt("nav_mode", 1).apply();

		buttons.add(navSearch);
		buttons.add(toggleTraffic);
		buttons.add(clearMap);
		buttons.add(mapDirections);

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
