package com.autosenseapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.autosenseapp.R;
import ca.efriesen.lydia_common.includes.Intents;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by eric on 2013-05-31.
 */
public class ContactView extends Activity implements AdapterView.OnItemClickListener {

	private static final String TAG = "contact view";
	private String[] PROJECTION;
	private String ORDER;
	private String SELECTION;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutInflater().inflate(R.layout.contact_view, null));

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Long contactId = extras.getLong("contact_id", 0);

			PROJECTION = new String[] {
				ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts.PHOTO_ID,
			};
			SELECTION = ContactsContract.Contacts._ID + " = " + contactId;

			Cursor contact = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, SELECTION, null, ORDER);

			// don't have valid info... quit
			if (!contact.moveToFirst()) {
				contact.close();
				finish();
			}

			TextView displayNameView = (TextView) findViewById(R.id.contact_display_name);
			displayNameView.setText(contact.getString(contact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));

			Cursor addresses = getContentResolver().query(
					ContactsContract.Data.CONTENT_URI,
					new String[]{
							ContactsContract.CommonDataKinds.StructuredPostal.STREET,
							ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
					},
					ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.CommonDataKinds.StructuredPostal.MIMETYPE + "=?",
					new String[] {
							String.valueOf(contactId), ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
					}, null);

			if (addresses != null && addresses.moveToFirst()) {
				Log.d(TAG, "got address");
				TextView formattedAddress = (TextView) findViewById(R.id.formatted_address);
				final String addressString = addresses.getString(addresses.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
				formattedAddress.setText(addressString);
				formattedAddress.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View view, MotionEvent motionEvent) {
						Log.d(TAG, "address " + addressString);
						Intent showOnMap = new Intent(Intents.SHOWONMAP);
						showOnMap.putExtra("formattedAddress", addressString);
						setResult(RESULT_OK, showOnMap);
						finish();
						return false;
					}
				});
			}

			Cursor photo = null;
			try {
				photo = getContentResolver().query(
					ContactsContract.Data.CONTENT_URI,
					new String[] {ContactsContract.CommonDataKinds.Photo.PHOTO},
					ContactsContract.Data._ID + "=?",
					new String[]{contact.getString(contact.getColumnIndex(ContactsContract.Contacts.PHOTO_ID))},
					null
				);

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			ImageView contactPhoto = (ImageView) findViewById(R.id.contact_photo);

			if (photo != null && photo.moveToFirst()) {
				byte[] photoBlob = photo.getBlob(photo.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
				final Bitmap photoBitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.length);
				contactPhoto.setBackground(new BitmapDrawable(getResources(), photoBitmap));
			} else {
				contactPhoto.setBackgroundResource(R.drawable.avatar_blank);
			}

			// get the list from the view
			ListView phoneNumbersList = (ListView) findViewById(R.id.phone_numbers);
			// create a new list for the phone numbers
			final List<ListViewEntry> phoneNumbers = new LinkedList<ListViewEntry>();
			// get all the phone numbers
			queryAllPhoneNumbersForContact(contactId, phoneNumbers);
			// set the adapter
			phoneNumbersList.setAdapter(new ContactListViewAdapter(phoneNumbers, ContactView.this));
			if (phoneNumbers.size() < 1) {
				LinearLayout phoneContainer = (LinearLayout) findViewById(R.id.phone_container);
				phoneContainer.setVisibility(View.GONE);
			}


			ListView emailsList = (ListView) findViewById(R.id.emails);
			final List<ListViewEntry> emails = new LinkedList<ListViewEntry>();
			queryAllEmailAddressesForContact(contactId, emails);
			emailsList.setAdapter(new ContactListViewAdapter(emails, ContactView.this));
			if (emails.size() < 1) {
				LinearLayout emailContainer = (LinearLayout) findViewById(R.id.email_container);
				emailContainer.setVisibility(View.GONE);
			}

			contact.close();
		}
		Log.d(TAG, "on create");
	}

	public void queryAllPhoneNumbersForContact(long contactId, List<ListViewEntry> content) {
		final String[] projection = new String[] {
				ContactsContract.CommonDataKinds.Phone.NUMBER,
				ContactsContract.CommonDataKinds.Phone.TYPE,
		};

		final Cursor phone = getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				projection,
				ContactsContract.Data.CONTACT_ID + "=?",
				new String[]{String.valueOf(contactId)},
				null);

		if (phone.moveToFirst()) {
			final int contactNumberColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
			final int contactTypeColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

			while (!phone.isAfterLast()) {
				final String number = PhoneNumberUtils.formatNumber(phone.getString(contactNumberColumnIndex));
				final int type = phone.getInt(contactTypeColumnIndex);
				content.add(new ListViewEntry(number, ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(type)));
				phone.moveToNext();
			}

		}
		phone.close();
	}


	public void queryAllEmailAddressesForContact(long contactId, List<ListViewEntry> content) {
		final String[] projection = new String[] {
				ContactsContract.CommonDataKinds.Email.DATA, // use Email.ADDRESS for API-Level 11+
				ContactsContract.CommonDataKinds.Email.TYPE
		};

		final Cursor email = getContentResolver().query(
				ContactsContract.CommonDataKinds.Email.CONTENT_URI,
				projection,
				ContactsContract.Data.CONTACT_ID + "=?",
				new String[]{String.valueOf(contactId)},
				null);

		if(email.moveToFirst()) {
			final int contactEmailColumnIndex = email.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
			final int contactTypeColumnIndex = email.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE);

			while(!email.isAfterLast()) {
				final String address = email.getString(contactEmailColumnIndex);
				final int type = email.getInt(contactTypeColumnIndex);
				content.add(new ListViewEntry(address, ContactsContract.CommonDataKinds.Email.getTypeLabelResource(type)));
				email.moveToNext();
			}

		}
		email.close();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		Log.d(TAG, "phone number clicked");
	}
}

class ListViewEntry {

	/** The destination of the entry e.g. a phone number or email address **/
	private final String destinationAddress;

	/** String resource describing the type of the entry e.g. Home **/
	private final int typeResource;

	public ListViewEntry(String number, int typeResource) {
		this.destinationAddress = number;
		this.typeResource = typeResource;
	}

	public String getDestinationAddress() {
		return destinationAddress;
	}

	public int getTypeResource() {
		return typeResource;
	}
}

class ContactListViewAdapter extends BaseAdapter implements ListAdapter {
	private final List<ListViewEntry> content;
	private final Activity activity;

	public ContactListViewAdapter(List<ListViewEntry> content, Activity activity) {
		this.content = content;
		this.activity = activity;
	}

	public int getCount() {
		return content.size();
	}

	public ListViewEntry getItem(int position) {
		return content.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView,	ViewGroup parent) {
		final LayoutInflater inflater = activity.getLayoutInflater();   // default layout inflater
		final View listEntry = inflater.inflate(R.layout.listview_two_line_entry, null); // initialize the layout from xml
		final TextView number = (TextView) listEntry.findViewById(R.id.numberPhoneNumber);
		final TextView type = (TextView) listEntry.findViewById(R.id.numberType);
		final ListViewEntry current = content.get(position);

		type.setText(activity.getString(current.getTypeResource()));
		number.setText(current.getDestinationAddress());

		return listEntry;
	}

}