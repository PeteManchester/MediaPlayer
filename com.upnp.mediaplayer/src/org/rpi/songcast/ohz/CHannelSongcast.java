package org.rpi.songcast.ohz;

import org.apache.log4j.Logger;
import org.rpi.playlist.ChannelPlayList;

public class CHannelSongcast extends ChannelPlayList {
	
	private Logger log = Logger.getLogger(this.getClass());

	public CHannelSongcast(String uri, String metadata, int id) {
		super(uri, metadata, id);
	}

}
