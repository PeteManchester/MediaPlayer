package org.rpi.player.events;

public class EventUpdateTrackMetaText implements EventBase {

	private String title = null;
	private String artist = null;
	private String metaData = "";
	private int id;

	// public EventUpdateTrackMetaData(Object source) {
	// super(source);
	// }

	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTUPDATETRACKMETATEXT;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/***
	 * Check if the Title is NULL
	 * 
	 * @return
	 */
	public boolean isTitleValid() {
		if (title == null) {
			return false;
		}
		return true;
	}

	/**
	 * @param title
	 *            the title to set
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

	/***
	 * Check if the Artist is NULL
	 * 
	 * @return
	 */
	public boolean isArtistValid() {
		if (artist == null) {
			return false;
		}
		return true;
	}

	/***
	 * Check if the Artist and Title is NULL
	 * 
	 * @return
	 */
	public boolean isArtistAndTitleValid() {
		if (artist == null || title == null) {
			return false;
		}
		return true;
	}

	/**
	 * @param artist
	 *            the artist to set
	 */
	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getMetaText() {
		return metaData;
	}

	public void setMetaText(String metaData) {
		this.metaData = metaData;
	}

	public void setCurrentTrackId(int id) {
		this.id = id;
	}

	public int getCurrentTrackId() {
		return id;
	}

}
