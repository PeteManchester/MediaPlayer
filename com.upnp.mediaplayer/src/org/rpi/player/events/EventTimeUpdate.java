package org.rpi.player.events;

import java.util.EventObject;

public class EventTimeUpdate extends EventObject {

	public EventTimeUpdate(Object source) {
		super(source);

	}
	
	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}

	private long time = 0;
	

}
