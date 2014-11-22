package com.autosenseapp.devices;

import java.io.Serializable;

/**
 * Created by eric on 2013-09-06.
 */
public class MJLJ implements Serializable{

	private static final String TAG = "lydia engine mjlj";

	private String advance;
	private String flags;
	private String load;
	private String rpm;
	private boolean running;

	public void setAdvance(String advance) {
		this.advance = advance;
	}

	public String getAdvance() {
		return advance;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public String getFlags() {
		return flags;
	}

	public void setLoad(String load) {
		this.load = load;
	}

	public String getLoad() {
		return load;
	}

	public void setRpm(String rpm) {
		this.rpm = rpm;
	}

	public String getRpm() {
		return rpm;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean getRunning() {
		return running;
	}
}
