package org.rpi.netty.songcast.ohz;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.apache.log4j.Logger;

public class OHZTest extends SimpleChannelInboundHandler<OHZMessage> {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, OHZMessage msg) throws Exception {
		//log.debug("Read");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext paramChannelHandlerContext, Throwable paramThrowable) throws Exception {
		log.debug("Exception");
	}
}
