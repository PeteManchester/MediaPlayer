package org.rpi.songcast.ohu.receiver;

import org.apache.log4j.Logger;
import org.rpi.songcast.common.SongcastMessage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class OHULeakCatcher extends SimpleChannelInboundHandler<SongcastMessage> {
	
	private Logger log = Logger.getLogger(this.getClass());

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SongcastMessage msg) throws Exception {
		try
		{
			int refCnt = msg.getData().refCnt();
			if(refCnt>0)
			{
				log.debug("Leak Caught: " + msg);
				msg.getData().release(refCnt);
			}			
		}
		catch(Exception e)
		{
			log.error("Error Catching Leak");
		}
		
	}

}
