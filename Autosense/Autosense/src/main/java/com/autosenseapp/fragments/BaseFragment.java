package com.autosenseapp.fragments;

import android.app.Fragment;
import android.os.Bundle;

import com.autosenseapp.AutosenseApplication;

/**
 * Created by eric on 2014-09-16.
 */
public abstract class BaseFragment extends Fragment {

	public void onCreate(Bundle saved) {
		super.onCreate(saved);

		((AutosenseApplication)getActivity().getApplication()).inject(this);
	}
}
