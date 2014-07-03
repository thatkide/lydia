package ca.efriesen.lydia.buttons;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import ca.efriesen.lydia.activities.ContactList;
import ca.efriesen.lydia.databases.Button;

/**
 * Created by eric on 2014-06-15.
 */
public class ContactsButton extends BaseButton {

	private Activity activity;

	public ContactsButton(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	public void onClick(View view, Button button) {
		activity.startActivity(new Intent(activity, ContactList.class));
	}
}
