package org.rpi.airplay.audio;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

public class AudioChannelRequestHandler extends ChannelInboundHandlerAdapter{

	private Logger log = Logger.getLogger(this.getClass());	

	public AudioChannelRequestHandler() {
	}
	

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(msg instanceof ByteBuf)
		{
			ByteBuf res = (ByteBuf)msg;
			res.release();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error(cause);
		ctx.close();
	}
	
	@Override	
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception{
		log.debug("AudioChannel Registered: " + ctx.name());
		super.channelRegistered(ctx);
	}

	@Override
	public void channelActive( ChannelHandlerContext ctx) throws Exception {
		log.debug("AudioChannel Actvie: " + ctx.name());
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.debug("AudioChannel Inactive: " + ctx.name());
		super.channelInactive(ctx);
	};

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx ) throws Exception {
		log.debug("AudioChannel Unregistered: " + ctx.name());
		super.channelUnregistered(ctx);
	}

}
