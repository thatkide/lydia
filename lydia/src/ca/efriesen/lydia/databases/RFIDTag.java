package ca.efriesen.lydia.databases;

/**
 * Created by eric on 2013-08-18.
 */
public class RFIDTag {

	private long id;
	private long tagNumber;
	private String description;
	private boolean enabled;

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return this.id;
	}

	public void setTagNumber(long tagNumber) {
		this.tagNumber = tagNumber;
	}

	public long getTagNumber() {
		return this.tagNumber;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean getEnabled() {
		return this.enabled;
	}

}
