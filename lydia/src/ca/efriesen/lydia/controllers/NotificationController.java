package ca.efriesen.lydia.controllers;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
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

	// display time in seconds.  more urgency will increase the time
	private int noteDisplayTime = 30;

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
						Bundle bundle = info.get(currentNote);
						Fragment newFragment = (Fragment) bundle.getSerializable("fragment");
						// replace the screen with the new note
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
						((NotificationInterface) newFragment).restoreFragment(states.get(currentNote));
						// post with the proper display time
						handler.postDelayed(this, getDisplayTime(bundle.getInt("priority")));
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

	// add a new notification to the stack
	// take a class and a priority
	public void addNotification(Class<? extends NotificationInterface> fragmentClass, Integer priority) {
		// create a new bundle to store locally
		Bundle bundle = new Bundle();
		try {
			// add the new instance of the fragment class passed
			bundle.putSerializable("fragment", fragmentClass.newInstance());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
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
		// get the bundle
		Bundle bundle = info.get(currentNote);
		// and the actual fragment
		Fragment newFragment = (Fragment) bundle.getSerializable("fragment");
		// show it
		FragmentTransactionExtended transactionExtended = new FragmentTransactionExtended(
				activity,
				fragmentManager.beginTransaction(),
				fragmentManager.findFragmentById(R.id.notification_bar),
				newFragment,
				R.id.notification_bar);
		transactionExtended.addTransition(FragmentTransactionExtended.FADE);
		transactionExtended.commit();
		// repost the handler to show the new fragment for the proper amount of time
		handler.removeCallbacks(runnable);
		handler.postDelayed(runnable, getDisplayTime(bundle.getInt("priority")));
	}

	// calculate the display time based on the priority
	private int getDisplayTime(int priority) {
		// turn seconds into milliseconds
		int time = (noteDisplayTime * 1000);
		// each level of priority will add an additional 5 seconds
		time += (priority*10*1000);
		return time;
	}
}
