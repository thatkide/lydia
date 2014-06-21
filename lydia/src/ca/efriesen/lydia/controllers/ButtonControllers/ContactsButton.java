package ca.efriesen.lydia.controllers.ButtonControllers;

import android.app.Activity;
import android.content.Intent;
import ca.efriesen.lydia.activities.ContactList;

/**
 * Created by eric on 2014-06-15.
 */
public class ContactsButton extends BaseButton {

	public static final String ACTION = "ContactsButton";

	private Activity activity;

	public ContactsButton(Activity activity) {
		this.activity = activity;
	}

	@Override
	public String getAction() {
		return ACTION;
	}

	@Override
	public void onClick() {
		activity.startActivity(new Intent(activity, ContactList.class));
	}

	@Override
	public String getDescription() {
		return "Open Contacts Manager";
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
