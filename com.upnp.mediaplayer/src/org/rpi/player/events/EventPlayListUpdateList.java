package org.rpi.player.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.rpi.channel.ChannelPlayList;

public class EventPlayListUpdateList implements EventBase {
	
	private List<ChannelPlayList> channels = new ArrayList<ChannelPlayList>();

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTPLAYLISTUPDATELIST;
	}

	/**
	 * @return the channels
	 */
	public CopyOnWriteArrayList<ChannelPlayList> getChannels() {
		return new CopyOnWriteArrayList<ChannelPlayList>(channels);
	}

	/**
	 * @param channels the channels to set
	 */
	public void setChannels(List<ChannelPlayList> channels) {
		this.channels = channels;
	}
}
