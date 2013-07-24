package ca.efriesen.lydia.includes;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import ca.efriesen.lydia.R;

import java.util.ArrayList;

/**
 * Created by eric on 2013-06-01.
 */
public class Helpers {

	public static String getContactDisplayNameByNumber(Context context, String number) {
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		String name = "?";

		ContentResolver contentResolver = context.getContentResolver();
		Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

		try {
			if (contactLookup != null && contactLookup.getCount() > 0) {
				contactLookup.moveToNext();
				name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
				//String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}

		return name;
	}

	public static void hideAllFragmentsBut(FragmentManager manager, ArrayList<Fragment> fragments) {
		// replace the 'dashboard_container' fragment with a new 'settings fragment'
		FragmentTransaction transaction = manager.beginTransaction();
		transaction
				.hide(manager.findFragmentById(R.id.home_screen_container_fragment))
				.hide(manager.findFragmentById(R.id.settings_fragment))
				.hide(manager.findFragmentById(R.id.home_screen_fragment))
				.hide(manager.findFragmentById(R.id.home_screen_fragment_two))
				.hide(manager.findFragmentById(R.id.music_fragment))
				.hide(manager.findFragmentById(R.id.map_container_fragment))
				.hide(manager.findFragmentById(R.id.phone_fragment))
				.hide(manager.findFragmentById(R.id.launcher_fragment));
		for (Fragment fragment : fragments) {
			transaction.show(fragment);
		}
		transaction.addToBackStack(null);
		transaction.commit();

	}

	public static void swapArrayElements(ArrayList list, int first, int second) {
		Object temp = list.set(first, list.get(second));
		list.set(second, temp);
	}
}
