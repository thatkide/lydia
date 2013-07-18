package ca.efriesen.lydia_common.media;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.io.Serializable;

/**
 * Created by eric on 2013-06-23.
 */
public class Artist extends Media implements Serializable {
	private int id;
	private String name;
	private String sortName;

	public void setCursorData(Cursor cursor) {
		setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)));
		setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int getId() {
		return id;
	}

	// set the name
	public void setName(String name) {
		this.name = name;
		// if the name has "the " in the beginning, move it to the end
		if (name.toLowerCase().startsWith("the ")) {
			name = name.substring(4, name.length()) + ", The";
		}
		this.sortName = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getSortName() {
		return sortName;
	}

	@Override
	public String toString() {
		return name;
	}
}
