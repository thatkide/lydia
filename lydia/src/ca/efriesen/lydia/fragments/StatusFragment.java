package ca.efriesen.lydia.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ca.efriesen.lydia.R;

import java.lang.Override;

/**
 * User: eric
 * Date: 2013-01-05
 * Time: 10:51 PM
 */
public class StatusFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.status_fragment, container, false);
	}

	private void destroyFragment() {
		getChildFragmentManager().beginTransaction().hide(this).commit();
	}
}