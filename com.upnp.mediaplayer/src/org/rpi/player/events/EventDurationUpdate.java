package org.rpi.player.events;


public class EventDurationUpdate implements EventBase {

	
	public EnumPlayerEvents getType()
	{
		return EnumPlayerEvents.EVENTDURATIONUPDATE;
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
	public void setDuration(long time) {
		this.duration = time;
	}

	private long duration = 0;

}
