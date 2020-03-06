package org.rpi.songcast.ohu.receiver;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class OHUMessageSlaveHandler extends SimpleChannelInboundHandler<OHUMessageSlave> {

	private Logger log = Logger.getLogger(this.getClass());
	private OHUChannelInitializer initializer = null;
	
	public OHUMessageSlaveHandler(OHUChannelInitializer initializer)
	{
		this.initializer = initializer;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, OHUMessageSlave msg) throws Exception {
		log.debug("Slave Message");
		try {
			if(msg instanceof OHUMessageSlave)
			{
				OHUMessageSlave slave = (OHUMessageSlave)msg;
				log.debug(slave.toString());
			}
		} catch (Exception e) {
			log.error("Error Releasing Slave ByteBuf");
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
