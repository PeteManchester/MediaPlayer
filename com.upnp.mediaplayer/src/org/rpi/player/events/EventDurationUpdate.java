package org.rpi.player.events;

import java.util.EventObject;

public class EventDurationUpdate extends EventObject {

	public EventDurationUpdate(Object source) {
		super(source);
		// TODO Auto-generated constructor stub
	}
	

	
	/**
	 * @return the time
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @param time the time to set
	 */
	public void setDuratoin(long time) {
		this.duration = time;
	}

	private long duration = 0;

}
