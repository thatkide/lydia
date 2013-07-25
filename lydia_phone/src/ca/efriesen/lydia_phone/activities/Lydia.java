package ca.efriesen.lydia_phone.activities;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import ca.efriesen.lydia_phone.R;
import ca.efriesen.lydia_phone.fragments.MediaControlsFragment;
import ca.efriesen.lydia_phone.fragments.SettingsFragment;
import ca.efriesen.lydia_phone.includes.TabsAdapter;
import ca.efriesen.lydia_phone.services.ManagerService;

/**
 * User: eric
 * Date: 2012-10-23
 * Time: 7:35 PM
 */
public class Lydia extends FragmentActivity {

	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;

	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private final static int REQUEST_ENABLE_BT = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.view_pager);
		setContentView(mViewPager);

		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);

		mTabsAdapter = new TabsAdapter(this, mViewPager);
		mTabsAdapter.addTab(actionBar.newTab().setText("Media"), MediaControlsFragment.class, null);
		mTabsAdapter.addTab(actionBar.newTab().setText("Settings"), SettingsFragment.class, null);

		// bluetooth not supported
		if (mBluetoothAdapter == null) {
			finish();
			return;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStart() {
		super.onStart();
		// we support bluetooth, but it's off
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBt, REQUEST_ENABLE_BT);
		} else {
			Intent startManager = new Intent(this, ManagerService.class);
			startService(startManager);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_ENABLE_BT: {
				// we weren't successful
				if (resultCode != RESULT_OK) {
					// exit the program
					finish();
					return;
				} else {
					Intent startManager = new Intent(this, ManagerService.class);
					startService(startManager);
				}
				break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.bluetooth_discoverable) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
		return false;
	}
}