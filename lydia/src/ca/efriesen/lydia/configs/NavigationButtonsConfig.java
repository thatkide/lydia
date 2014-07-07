package ca.efriesen.lydia.configs;

import java.util.ArrayList;
import java.util.List;

import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.buttons.appButtons.NavigationButton;
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
	public List<Button> getButtons() {
		List<Button> buttons = new ArrayList<Button>();
		int area = 0;

		Button navSearch = new Button();
		navSearch.setAction(NavSearchButton.class.getSimpleName());
		navSearch.setButtonType(type);
		navSearch.setDisplayArea(area);
		navSearch.setGroup(group);
		navSearch.setPosition(0);
		navSearch.setTitle("Search");
		navSearch.setUsesDrawable(false);
		navSearch.setDrawable("blank");

		Button toggleTraffic = new Button();
		toggleTraffic.setAction(ToggleTrafficButton.class.getSimpleName());
		toggleTraffic.setButtonType(type);
		toggleTraffic.setDisplayArea(area);
		toggleTraffic.setGroup(group);
		toggleTraffic.setPosition(1);
		toggleTraffic.setTitle("Toggle Traffic");
		toggleTraffic.setUsesDrawable(false);
		toggleTraffic.setDrawable("blank");

		Button clearMap = new Button();
		clearMap.setAction(ToggleTrafficButton.class.getSimpleName());
		clearMap.setButtonType(type);
		clearMap.setDisplayArea(area);
		clearMap.setGroup(group);
		clearMap.setPosition(2);
		clearMap.setTitle("Clear");
		clearMap.setUsesDrawable(false);
		clearMap.setDrawable("blank");



		buttons.add(navSearch);
		buttons.add(toggleTraffic);
		buttons.add(clearMap);
		// toggle traffic
		// clear map
		// driviving/walking/biking
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
