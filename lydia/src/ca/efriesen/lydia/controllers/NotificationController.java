package ca.efriesen.lydia.controllers;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import com.desarrollodroide.libraryfragmenttransactionextended.FragmentTransactionExtended;
import java.util.ArrayList;
import java.util.List;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.interfaces.NotificationInterface;

/**
 * Created by eric on 2014-07-14.
 */
public class NotificationController {
	private static final String TAG = NotificationController.class.getSimpleName();

	private Activity activity;
	private FragmentManager fragmentManager;
	private final Handler handler = new Handler();
	private Runnable runnable;

	// array lists to store info about the fragments, priority and such
	private List<String> names = new ArrayList<String>();
	private List<Bundle> info = new ArrayList<Bundle>();
	private List<Bundle> states = new ArrayList<Bundle>();
	private int currentNote = 0;

	public NotificationController(final Activity activity) {
		this.activity = activity;
		this.fragmentManager = activity.getFragmentManager();
		// create the new runnable.  this does the job of rotating the notes
		runnable = new Runnable() {
			@Override
			public void run() {
				// if we have more than one note to show
				if (info.size() > 1) {
					try {
						// save the state of the currently displayed note.  this is the "saveFragment" method.  it stores it here until next time it's needed
						((NotificationInterface) fragmentManager.findFragmentById(R.id.notification_bar)).saveFragment(states.get(currentNote));

						// increment counter
						currentNote++;
						// if we went over the size, start over
						if (currentNote >= info.size()) {
							currentNote = 0;
						}

						// get next note screen
						replaceFragment(currentNote);

					} catch (NullPointerException e) {
						handler.removeCallbacks(runnable);
					} catch (IllegalStateException e) {
						handler.removeCallbacks(runnable);
					}
				}
			}
		};
		// initial screen, it's normal priority
		handler.postDelayed(runnable, getDisplayTime(NotificationInterface.PRIORITY_NORMAL));
	}

	public void onPause() {
		handler.removeCallbacks(runnable);
	}

	public void onResume() {
		handler.postDelayed(runnable, getDisplayTime(NotificationInterface.PRIORITY_NORMAL));
	}

	// add a new notification to the stack
	// take a class and a priority
	public void addNotification(Class<? extends NotificationInterface> fragmentClass, Integer priority) {
		// create a new bundle to store locally
		Bundle bundle = new Bundle();
		// add the new instance of the fragment class passed
		bundle.putString("fragment", fragmentClass.getCanonicalName());
		// store the priority
		bundle.putInt("priority", priority);
		// add the arrays
		names.add(fragmentClass.getSimpleName()); // just for index searching
		info.add(bundle); // fragment and priority based on above index
		states.add(new Bundle()); // any local data the fragment needs to be passed from savefragment to restorefragment
	}

	// remove a fragment from the list
	public void removeNotification(Class<? extends NotificationInterface> fragmentClass) {
		int index = names.indexOf(fragmentClass.getSimpleName());
		names.remove(index);
		info.remove(index);
		states.remove(index);
	}

	// set the passed notification class to active
	public void setNotification(Class<? extends NotificationInterface> fragmentClass) {
		// get the index
		currentNote = names.indexOf(fragmentClass.getSimpleName());
		// and the replace the fragment
		replaceFragment(currentNote);
	}

	// calculate the display time based on the priority
	private int getDisplayTime(int priority) {
		// turn seconds into milliseconds
		return  (priority * 1000);
	}

	private void replaceFragment(int currentNote) {
		Bundle bundle = info.get(currentNote);
		try {
			Fragment newFragment = (Fragment) Class.forName(bundle.getString("fragment")).newInstance();
			// show it
			FragmentTransactionExtended transactionExtended = new FragmentTransactionExtended(
					activity,
					fragmentManager.beginTransaction(),
					fragmentManager.findFragmentById(R.id.notification_bar),
					newFragment,
					R.id.notification_bar);
			transactionExtended.addTransition(FragmentTransactionExtended.FADE);
			transactionExtended.commit();

			// commit is async so we must force it.  do the commit, then do the restore fragment callback
			fragmentManager.executePendingTransactions();
			// pass the bundle to the fragment just inflated.
			try {
				((NotificationInterface) newFragment).restoreFragment(states.get(currentNote));
			} catch (NullPointerException e) {}

			// repost the handler to show the new fragment for the proper amount of time
			handler.removeCallbacks(runnable);
			handler.postDelayed(runnable, getDisplayTime(bundle.getInt("priority")));

		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
