package com.autosenseapp.callbacks;

import com.autosenseapp.databases.Button;
import java.util.List;

/**
 * Created by eric on 2014-06-28.
 */
public interface DrawScreenCallback {
	public void drawScreen(List<Button> buttons);
	public boolean fullDraw();
}
