package org.rpi.songcast.ohz.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

import org.apache.log4j.Logger;

public class OHZTest extends SimpleChannelInboundHandler<OHZZoneUriMessage> {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, OHZZoneUriMessage msg) throws Exception {
		try {
			log.debug("OHZTest:" + msg.toString());
	       //msg.getData().release();
	    } finally {
	    	log.debug("OHZTest: Release ByteBuffer");
	        ReferenceCountUtil.release(msg);
	    }
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext paramChannelHandlerContext, Throwable paramThrowable) throws Exception {
		log.debug("Exception");
	}
}
