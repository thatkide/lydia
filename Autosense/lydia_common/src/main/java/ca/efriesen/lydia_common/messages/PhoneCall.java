package ca.efriesen.lydia_common.messages;

import java.io.Serializable;

/**
 * User: eric
 * Date: 2013-07-24
 * Time: 12:38 PM
 */
public class PhoneCall extends Message implements Serializable {
	private int id;
	private String fromNumber;
	private String toNumber;
	private int state;

	public int getId() {
		return id;
	}

	private void setId(int id) {
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

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}
