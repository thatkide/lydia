package ca.efriesen.lydia.activities;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import ca.efriesen.lydia.R;

/**
 * Created by eric on 2013-06-01.
 */
public class ContactList extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter mAdapter;
	private String[] PROJECTION;
	private String ORDER;
	private String SELECTION;

	private final int contactLoader = 2;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutInflater().inflate(R.layout.contact_list, null));

		PROJECTION = new String[] {
				ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts.IN_VISIBLE_GROUP
		};
		SELECTION = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'";
		ORDER = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

		// init loader (unique id, args for the loader constructor, callback implementation)
		getLoaderManager().initLoader(contactLoader, null, this);
		String[] fromColumns = {ContactsContract.Contacts.DISPLAY_NAME};
		int[] toViews = {android.R.id.text1};

		mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, fromColumns, toViews, 0);

		ListView contactsList = (ListView) findViewById(R.id.contact_list);
		contactsList.setAdapter(mAdapter);

		contactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int positions, long id) {
				Intent startContactView = new Intent(getApplicationContext(), ContactView.class);
				startContactView.putExtra("contact_id", id);
				startActivityForResult(startContactView, 1);
			}
		});

		// contact filter input text
		final EditText contactFilter = (EditText) findViewById(R.id.contact_filter);

		// listener for edit text
		contactFilter.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

			@Override
			public void afterTextChanged(Editable editable) {
				// get the text entered
				String text = contactFilter.getText().toString();
				// update the selection
				SELECTION = ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%" + text + "%' AND " +
						ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'";
				restartLoader();
			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// check the codes
		switch (requestCode) {
			case 1: {
				sendBroadcast(intent);
				finish();
			}
		}
	}

				@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(getApplicationContext(), ContactsContract.Contacts.CONTENT_URI, PROJECTION, SELECTION, null, ORDER);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		getLoaderManager().restartLoader(contactLoader, null, this);
	}

	final void restartLoader() {
		getLoaderManager().restartLoader(contactLoader, null, this);
	}
}
