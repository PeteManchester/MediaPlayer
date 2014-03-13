package org.rpi.channel;

import org.apache.log4j.Logger;

public class ChannelSongcast extends ChannelBase {
	
	private Logger log = Logger.getLogger(this.getClass());

	public ChannelSongcast(String uri, String metadata, int id) {
		super(uri, metadata, id);
	}

}
