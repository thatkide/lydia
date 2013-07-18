package ca.efriesen.lydia_common.media;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.io.Serializable;

/**
 * Created by eric on 2013-06-24.
 */
public class Media implements Serializable {
	private int id;
	private String name;

	public void setCursorData(Cursor cursor) {
		setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
		setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)) + ": " + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

}
