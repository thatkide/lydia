package ca.efriesen.lydia.callbacks;

import java.util.List;

import ca.efriesen.lydia.databases.Button;

/**
 * Created by eric on 2014-07-06.
 */
public interface ButtonCheckerCallback {
	public List<Button> getButtons();
	public int getGroup();
	public int getType();
}
