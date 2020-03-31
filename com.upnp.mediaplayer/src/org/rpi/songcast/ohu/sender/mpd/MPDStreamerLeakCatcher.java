package org.rpi.songcast.ohu.sender.mpd;



import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MPDStreamerLeakCatcher extends SimpleChannelInboundHandler<Object> {
	
	private Logger log = Logger.getLogger(this.getClass());

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, Object arg1) throws Exception {
		log.debug("Log Got Here: " + arg1);		
	}
	
	

}
