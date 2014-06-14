package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.devices.Master;
import ca.efriesen.lydia.includes.Helpers;

/**
 * Created by eric on 2014-06-13.
 */
public class RadioFragment extends Fragment {

	private static final String TAG = "lydia radio fragment";

	private Activity activity;

	private Button seekDown;
	private Button seekUp;
	private Button fox;
	private Button rock101;

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		this.activity = getActivity();

		seekDown = (Button) activity.findViewById(R.id.radio_seek_down);
		seekUp = (Button) activity.findViewById(R.id.radio_seek_up);
		fox = (Button) activity.findViewById(R.id.radio_fox);
		rock101 = (Button) activity.findViewById(R.id.radio_101);

		seekDown.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				byte data[] = {1};
				Master.writeData(activity, Master.RADIOSEEKDOWN, data);
				Log.d(TAG, "seek down");
			}
		});

		seekUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				byte data[] = {1};
				Master.writeData(activity, Master.RADIOSEEKUP, data);
				Log.d(TAG, "seek up");
			}
		});

		fox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				byte data[] = {Helpers.highByte(993), Helpers.lowByte(993)};
				Master.writeData(activity, Master.RADIOSETCHANNEL, data);
			}
		});

		rock101.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				byte data[] = {Helpers.highByte(1011), Helpers.lowByte(1011)};
				Master.writeData(activity, Master.RADIOSETCHANNEL, data);
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.radio_fragment, container, false);
	}
}
