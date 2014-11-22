package com.autosenseapp.databases;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;

/**
 * Created by eric on 2013-08-02.
 */
public class BluetoothDeviceDataSource {
	private SQLiteDatabase database;
	private BluetoothDeviceOpenHelper dbHelper;

	private static final String TAG = "lydia bluetooth devices database";

	public BluetoothDeviceDataSource(Context context) {
		dbHelper = new BluetoothDeviceOpenHelper(context);
	}

	public void open() throws SQLiteException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void addDevice(BluetoothDevice device) {
		ContentValues values = new ContentValues();
		values.put(BluetoothDeviceOpenHelper.ADDRESS, device.getAddress());

		database.insert(BluetoothDeviceOpenHelper.TABLE_NAME, null, values);
	}

	public void removeDevice(BluetoothDevice device) {
		database.delete(BluetoothDeviceOpenHelper.TABLE_NAME, BluetoothDeviceOpenHelper.ADDRESS + " = " + DatabaseUtils.sqlEscapeString(device.getAddress()), null);
	}

	public ArrayList<BluetoothDevice> getAllDevices() {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

		Cursor cursor = database.query(
				BluetoothDeviceOpenHelper.TABLE_NAME,
				new String[] {BluetoothDeviceOpenHelper.COLUMN_ID, BluetoothDeviceOpenHelper.ADDRESS},
				null,
				null,
				null,
				null,
				null
		);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			devices.add(adapter.getRemoteDevice(cursor.getString(cursor.getColumnIndex(BluetoothDeviceOpenHelper.ADDRESS))));
			cursor.moveToNext();
		}
		return devices;
	}

	public boolean isDeviceInDB(BluetoothDevice device) {
		Cursor cursor = database.query(
				BluetoothDeviceOpenHelper.TABLE_NAME,
				new String[] {BluetoothDeviceOpenHelper.COLUMN_ID, BluetoothDeviceOpenHelper.ADDRESS},
				BluetoothDeviceOpenHelper.ADDRESS + " = " + DatabaseUtils.sqlEscapeString(device.getAddress()),
				null,
				null,
				null,
				null
		);
		if (cursor.getCount() > 0) {
			cursor.close();
			return true;
		}
		cursor.close();
		return false;
	}

}
