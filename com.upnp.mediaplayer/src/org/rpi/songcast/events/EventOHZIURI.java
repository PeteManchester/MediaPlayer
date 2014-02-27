package org.rpi.songcast.events;


public class EventOHZIURI implements EventSongCastBase {
	
	private String uri = "";
	private String zone = "";

	@Override
	public EnumSongCastEvents getType() {
		return EnumSongCastEvents.EVENT_OHZ_URI;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the zone
	 */
	public String getZone() {
		return zone;
	}

	/**
	 * @param zone the zone to set
	 */
	public void setZone(String zone) {
		this.zone = zone;
	}

}
