package org.rpi.songcast.ohu;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class OHULeakCatcher extends SimpleChannelInboundHandler<OHUMessageTrack> {
	
	private Logger log = Logger.getLogger(this.getClass());

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, OHUMessageTrack msg) throws Exception {
		try
		{
			int refCnt = msg.getData().refCnt();
			if(refCnt>0)
			{
				msg.getData().release(refCnt);
			}			
		}
		catch(Exception e)
		{
			log.error("Error Catching Leak");
		}
		
	}

}
