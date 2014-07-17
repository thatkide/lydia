package ca.efriesen.lydia.interfaces;

import android.os.Bundle;

import java.io.Serializable;

/**
 * Created by eric on 2014-07-14.
 */
public interface NotificationInterface extends Serializable {
	public static final int PRIORITY_NORMAL = 0;
	public static final int PRIORITY_HIGH = 1;
	public static final int PRIORITY_URGANT = 2;
	public static final int PRIORITY_VERY_URGANT = 3;

	public void saveFragment(Bundle bundle);
	public void restoreFragment(Bundle bundle);
}
