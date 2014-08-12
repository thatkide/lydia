package ca.efriesen.lydia.databases;

import java.io.Serializable;

/**
 * Created by eric on 2013-08-18.
 */
public class RFIDTag implements Serializable{

	private long id;
	private long tagNumber;
	private String description;
	private boolean enabled;
	private boolean startCar;
	private boolean unlockDoors;
	private int eepromAddress;

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

	public void setStartCar(boolean startCar) {
		this.startCar = startCar;
	}

	public boolean getStartCar() {
		return startCar;
	}

	public void setUnlockDoors(boolean unlockDoors) {
		this.unlockDoors = unlockDoors;
	}

	public boolean getUnlockDoors() {
		return unlockDoors;
	}

	public void setEepromAddress(int eepromAddress) {
		this.eepromAddress = eepromAddress;
	}

	public int getEepromAddress() {
		return eepromAddress;
	}

}
