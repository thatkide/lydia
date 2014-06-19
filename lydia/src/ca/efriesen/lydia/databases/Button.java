package ca.efriesen.lydia.databases;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by eric on 2014-06-15.
 */
public class Button implements Parcelable{

	private int id;
	private int displayArea;
	private int position;
	private String title;
	private String  action;
	private String drawable;
	private boolean usesDrawable;

	public Button() {}

	public Button(Parcel parcel) {
		this.id = parcel.readInt();
		this.displayArea = parcel.readInt();
		this.position = parcel.readInt();
		this.title = parcel.readString();
		this.action = parcel.readString();
		this.drawable = parcel.readString();
		this.usesDrawable = parcel.readInt() > 0;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setDisplayArea(int displayArea) {
		this.displayArea = displayArea;
	}

	public int getDisplayArea() {
		return displayArea;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getAction() {
		return action;
	}

	public void setDrawable(String drawable) {
		this.drawable = drawable;
	}

	public String getDrawable() {
		return drawable;
	}

	public void setUsesDrawable(boolean usesDrawable) {
		this.usesDrawable = usesDrawable;
	}

	public boolean getUsesDrawable() {
		return usesDrawable;
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeInt(id);
		parcel.writeInt(displayArea);
		parcel.writeInt(position);
		parcel.writeString(title);
		parcel.writeString(action);
		parcel.writeString(drawable);
		parcel.writeInt(usesDrawable ? 1 : 0);
	}

	public static final Creator<Button> CREATOR = new Creator<Button>() {
		@Override
		public Button createFromParcel(Parcel source) {
			return new Button(source);
		}

		@Override
		public Button[] newArray(int size) {
			return new Button[size];
		}
	};
}
