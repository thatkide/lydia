package ca.efriesen.lydia.databases;

/**
 * Created by eric on 2013-06-02.
 */
public class Message {
	public static final String TYPE_PHONE = "phone";
	public static final String TYPE_SMS = "sms";

	private long id;
	private String message;
	private String phone_number;
	private long time_received;
	private String type;
	private Boolean from_me;


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPhoneNumber() {
		return phone_number;
	}

	public void setPhoneNumber(String phone_number) {
		this.phone_number = phone_number;
	}

	public String getTimeReceived() {
		return String.valueOf(time_received);
	}

	public void setTimeReceived(long time_received) {
		this.time_received = time_received;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean getFromMe() {
		return from_me;
	}

	public void setFromMe(boolean from_me) {
		this.from_me = from_me;
	}

	@Override
	public String toString() {
		return message;
	}
}
