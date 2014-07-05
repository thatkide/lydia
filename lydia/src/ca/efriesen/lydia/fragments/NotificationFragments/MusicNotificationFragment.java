package ca.efriesen.lydia.fragments.NotificationFragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.efriesen.lydia.R;

/**
 * Created by eric on 2014-07-04.
 */
public class MusicNotificationFragment extends Fragment {

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.notification_music_bar, container, false);
	}
}
