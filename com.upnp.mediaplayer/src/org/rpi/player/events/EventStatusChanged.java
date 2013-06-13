package org.rpi.player.events;

import org.rpi.playlist.CustomTrack;

public class EventStatusChanged implements EventBase {
	
	public EnumPlayerEvents getType()
	{
		return EnumPlayerEvents.EVENTSTATUSCHANGED;
	}
	
	private CustomTrack current_track = null;

//	public EventStatusChanged(Object source) {
//		super(source);
//	}
	
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	private String status = "";

	public void setTrack(CustomTrack current_track) {
		this.current_track = current_track;
	}
	
	public CustomTrack getTrack()
	{
		return current_track;
	}

}
