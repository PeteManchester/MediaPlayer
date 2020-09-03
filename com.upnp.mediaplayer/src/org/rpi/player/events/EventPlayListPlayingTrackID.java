package org.rpi.player.events;

import org.rpi.channel.ChannelBase;

public class EventPlayListPlayingTrackID implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTPLAYLISTPLAYINGTRACKID;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private int id = -99;
	private ChannelBase channel = null;

	public void setChannel(ChannelBase t) {
		this.channel = t;		
	}
	
	public ChannelBase getChannel() {
		return channel;
	}

}
