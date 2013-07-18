package ca.efriesen.lydia.devices;

import android.content.Context;

/**
 * Created by eric on 2013-05-28.
 */
public class LightSensor extends Device {

	public LightSensor(Context context, int id, String intentFilter) {
		super(context, id, intentFilter);
	}

	@Override
	public void cleanUp() {

	}

}
