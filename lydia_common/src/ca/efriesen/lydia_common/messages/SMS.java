package ca.efriesen.lydia_common.messages;

import java.io.Serializable;

/**
 * User: eric
 * Date: 2013-07-24
 * Time: 11:23 AM
 */
public class SMS extends Message implements Serializable {
	private int id;
	private String fromNumber;
	private String toNumber;
	private String message;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFromNumber() {
		return fromNumber;
	}

	public void setFromNumber(String fromNumber) {
		this.fromNumber = fromNumber;
	}

	public String getToNumber() {
		return toNumber;
	}

	public void setToNumber(String toNumber) {
		this.toNumber = toNumber;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
