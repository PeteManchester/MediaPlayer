package org.rpi.player.events;

import java.util.EventObject;

import org.rpi.playlist.CustomTrack;

public class EventTrackChanged extends EventObject {
	
	public EventTrackChanged(Object source) {
		super(source);
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
