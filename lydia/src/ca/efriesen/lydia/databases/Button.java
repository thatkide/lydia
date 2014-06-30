package ca.efriesen.lydia.databases;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by eric on 2014-06-15.
 */
public class Button implements Parcelable{

	private int id;
	private int buttonType;
	private int displayArea;
	private int position;
	private String title;
	private String  action;
	private String drawable;
	private boolean usesDrawable;
	private String extraData;

	public Button() {}

	public Button(Parcel parcel) {
		this.id = parcel.readInt();
		this.buttonType = parcel.readByte();
		this.displayArea = parcel.readInt();
		this.position = parcel.readInt();
		this.title = parcel.readString();
		this.action = parcel.readString();
		this.drawable = parcel.readString();
		this.usesDrawable = parcel.readInt() > 0;
		this.extraData = parcel.readString();
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setButtonType(int type) { this.buttonType = type; }

	public int getButtonType() { return buttonType; }

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

	public void setExtraData(String extraData) {
		this.extraData = extraData;
	}

	public String getExtraData() {
		return extraData;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeInt(id);
		parcel.writeInt(buttonType);
		parcel.writeInt(displayArea);
		parcel.writeInt(position);
		parcel.writeString(title);
		parcel.writeString(action);
		parcel.writeString(drawable);
		parcel.writeInt(usesDrawable ? 1 : 0);
		parcel.writeString(extraData);
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
