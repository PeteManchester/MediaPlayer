package org.rpi.player.events;


public class EventTimeUpdate implements EventBase {

//	public EventTimeUpdate(Object source) {
//		super(source);
//
//	}
	
	public EnumPlayerEvents getType()
	{
		return EnumPlayerEvents.EVENTTIMEUPDATED;
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
