package org.rpi.netty.songcast.ohu;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class OHUMessageMetaTextHandler extends SimpleChannelInboundHandler<OHUMessageMetaText> {

	private Logger log  = Logger.getLogger(this.getClass());
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, OHUMessageMetaText msg) throws Exception {
		log.debug("MetaText");
		try {
			if(msg instanceof OHUMessageMetaText)
			{
				OHUMessageMetaText meta_text = (OHUMessageMetaText)msg;
				log.debug(meta_text.toString());
			}
		} catch (Exception e) {
			log.error("Error Releasing MetaText ByteBuf");
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error(cause);
		ctx.close();
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Registered: " + ctx.name());
		super.channelRegistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Actvie: " + ctx.name());
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Inactive: " + ctx.name());
		super.channelInactive(ctx);
	};

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Unregistered: " + ctx.name());
		super.channelUnregistered(ctx);
	}

}
