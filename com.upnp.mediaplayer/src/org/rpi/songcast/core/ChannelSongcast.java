package org.rpi.songcast.core;

import org.apache.log4j.Logger;
import org.rpi.playlist.ChannelPlayList;

public class ChannelSongcast extends ChannelPlayList {
	
	private Logger log = Logger.getLogger(this.getClass());

	public ChannelSongcast(String uri, String metadata, int id) {
		super(uri, metadata, id);
	}

}
