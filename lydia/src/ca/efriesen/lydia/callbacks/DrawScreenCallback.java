package ca.efriesen.lydia.callbacks;

import ca.efriesen.lydia.databases.Button;
import java.util.List;

/**
 * Created by eric on 2014-06-28.
 */
public interface DrawScreenCallback {
	public void drawScreen(List<Button> buttons);
	public boolean fullDraw();
}
