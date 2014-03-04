package org.rpi.player.events;

import org.rpi.playlist.ChannelPlayList;

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
	public ChannelPlayList getTrack() {
		return track;
	}

	/**
	 * @param track the track to set
	 */
	public void setTrack(ChannelPlayList track) {
		this.track = track;
	}
	
	public String getMPD_id() {
		return mpd_id;
	}

	public void setMPD_id(String mpd_id) {
		this.mpd_id = mpd_id;
	}

	private String mpd_id = "";
	

	private ChannelPlayList track = null;



}
