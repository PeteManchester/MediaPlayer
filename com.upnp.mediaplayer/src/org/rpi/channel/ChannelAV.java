package org.rpi.channel;

import org.apache.log4j.Logger;

public class ChannelAV extends ChannelBase {

	private static final Logger LOG = Logger.getLogger(ChannelPlayList.class);	
	public ChannelAV(String uri, String metadata, int id) {
		super(uri, metadata, id);
	}

}
