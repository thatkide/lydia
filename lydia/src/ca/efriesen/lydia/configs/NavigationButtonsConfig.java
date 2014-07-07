package ca.efriesen.lydia.configs;

import java.util.ArrayList;
import java.util.List;

import ca.efriesen.lydia.buttons.BaseButton;
import ca.efriesen.lydia.buttons.NavigationButton;
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

		// create all the buttons we want in the admin panel
		Button navSearch = new Button();
		navSearch.setAction(NavigationButton.class.getSimpleName());
		navSearch.setButtonType(type);
		navSearch.setDisplayArea(area);
		navSearch.setGroup(group);
		navSearch.setPosition(0);
		navSearch.setTitle("Search");
		navSearch.setUsesDrawable(false);
		navSearch.setDrawable("blank");

		buttons.add(navSearch);
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
