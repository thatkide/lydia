package ca.efriesen.lydia.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.activities.ContactList;

/**
 * User: eric
 * Date: 2013-03-13
 * Time: 10:00 PM
 */
public class HomeScreenTwoFragment extends Fragment{

	Activity activity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// hide ourself on create
		getFragmentManager().beginTransaction().hide(getFragmentManager().findFragmentById(R.id.home_screen_fragment_two)).commit();

		return inflater.inflate(R.layout.home_screen_two_fragment, container, false);
	}

	// if the back button is pressed while we're visible, go back to home screen one
	public boolean onBackPressed() {
		FragmentManager manager = getFragmentManager();
		manager.beginTransaction()
				.hide(manager.findFragmentById(R.id.home_screen_fragment_two))
				.show(manager.findFragmentById(R.id.home_screen_fragment))
				.addToBackStack(null)
				.commit();
		return true;
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		activity = getActivity();

		Button contacts = (Button) activity.findViewById(R.id.contacts);
		contacts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				activity.startActivity(new Intent(activity, ContactList.class));
			}
		});
	}
}