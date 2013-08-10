package org.rpi.player.events;

import org.rpi.playlist.CustomTrack;

public class EventTrackChanged implements EventBase {
	
//	public EventTrackChanged(Object source) {
//		super(source);
//	}
	
	public EnumPlayerEvents getType()
	{
		return EnumPlayerEvents.EVENTTRACKCHANGED;
	}
	
	/**
	 * @return the track
	 */
	public CustomTrack getTrack() {
		return track;
	}

	/**
	 * @param track the track to set
	 */
	public void setTrack(CustomTrack track) {
		this.track = track;
	}

	private CustomTrack track = null;



}
