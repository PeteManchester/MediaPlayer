package org.rpi.player.events;

import java.util.EventObject;

public class EventUpdateTrackMetaData extends EventObject {
	

	private String title = null;
	private String artist = null;

	public EventUpdateTrackMetaData(Object source) {
		super(source);
	}
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the artist
	 */
	public String getArtist() {
		return artist;
	}

	/**
	 * @param artist the artist to set
	 */
	public void setArtist(String artist) {
		this.artist = artist;
	}


}
