package com.autosenseapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import com.autosenseapp.AutosenseApplication;
import butterknife.ButterKnife;

/**
 * Created by eric on 2014-09-16.
 */
public abstract class BaseFragment extends Fragment {

	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		((AutosenseApplication)getActivity().getApplication()).inject(this);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// clear the views
		ButterKnife.reset(this);
	}

}
