package ca.efriesen.lydia.fragments.NotificationFragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.efriesen.lydia.R;
import ca.efriesen.lydia.devices.Master;
import ca.efriesen.lydia.interfaces.NotificationInterface;

/**
 * Created by eric on 2014-07-15.
 */
public class TemperatureNotificationFragment extends Fragment implements NotificationInterface {

	private Activity activity;
	private TextView outsideTemp;
	private TextView insideTemp;
	private int inside = 0;
	private int outside = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.notification_temperature, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		activity = getActivity();
		// ensure out temperature info is updated
		byte[] data = {};
		Master.writeData(activity, Master.GETTEMP, data);
		// init the text views
		outsideTemp = (TextView) getActivity().findViewById(R.id.outside_temperature);
		insideTemp = (TextView) getActivity().findViewById(R.id.inside_temperature);
	}

	@Override
	public void onStart() {
		super.onStart();
		// register the broadcast receivers.  they get updated temp info from the arduino
		activity.registerReceiver(insideTemperatureReceiver, new IntentFilter(Master.INSIDETEMPERATURE));
		activity.registerReceiver(outsideTemperatureReceiver, new IntentFilter(Master.OUTSIDETEMPERATURE));
	}

	@Override
	public void onStop() {
		super.onStop();
		// unregister the receivers
		try {
			activity.unregisterReceiver(insideTemperatureReceiver);
		} catch (Exception e) {}
		try {
			activity.unregisterReceiver(outsideTemperatureReceiver);
		} catch (Exception e) {}
	}

	private BroadcastReceiver insideTemperatureReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// update the local var and text view
			inside = intent.getIntExtra(Master.INSIDETEMPERATURE, 0);
			insideTemp.setText(activity.getString(R.string.inside_temp) + ": " + inside + "\u2103");
		}
	};

	private BroadcastReceiver outsideTemperatureReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// update the local var and text view
			outside = intent.getIntExtra(Master.OUTSIDETEMPERATURE, 0);
			outsideTemp.setText(activity.getString(R.string.outside_temp) + ": " + outside + "\u2103");
		}
	};

	@Override
	public void saveFragment(Bundle bundle) {
		// put the temperatures into the bundle for next startup
		bundle.putInt("inside", inside);
		bundle.putInt("outside", outside);
	}

	@Override
	public void restoreFragment(Bundle bundle) {
		// ensure out temperature info is updated
		byte[] data = {};
		Master.writeData(activity, Master.GETTEMP, data);
		// use the passed info to show the temps.  anything updated will overwrite via the broadcast receiver
		insideTemp.setText(activity.getString(R.string.inside_temp) + ": " + bundle.getInt("inside") + "\u2103");
		outsideTemp.setText(activity.getString(R.string.outside_temp) + ": " + bundle.getInt("outside") + "\u2103");
	}
}
