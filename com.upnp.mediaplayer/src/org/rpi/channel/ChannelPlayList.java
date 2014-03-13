package org.rpi.channel;

import org.apache.log4j.Logger;

public class ChannelPlayList extends ChannelBase {

    private static final Logger LOG = Logger.getLogger(ChannelPlayList.class);


    public ChannelPlayList(String uri, String metadata, int id) {
		super(uri, metadata, id);
    }

}