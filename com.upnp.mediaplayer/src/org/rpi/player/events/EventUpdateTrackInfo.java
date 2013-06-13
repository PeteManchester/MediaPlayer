package org.rpi.player.events;

import org.rpi.mplayer.TrackInfo;


public class EventUpdateTrackInfo implements EventBase {

private TrackInfo trackInfo;

//	public EventUpdateTrackInfo(Object source) {
//		super(source);
//	}
	
	public EnumPlayerEvents getType()
	{
		return EnumPlayerEvents.EVENTUPDATETRACKINFO;
	}

	public void setTrackInfo(TrackInfo trackInfo) {
		this.trackInfo = trackInfo;
		
	}
	
	public TrackInfo getTrackInfo()
	{
		return trackInfo;
	}
	
}
