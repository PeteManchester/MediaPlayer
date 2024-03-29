package org.rpi.songcast.ohu.receiver.handlers;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.receiver.messages.OHUMessageSlave;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class OHUMessageSlaveHandler extends SimpleChannelInboundHandler<OHUMessageSlave> {

	private Logger log = Logger.getLogger(this.getClass());
	
	public OHUMessageSlaveHandler()
	{

	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, OHUMessageSlave msg) throws Exception {
		log.debug("Slave Message in OHUMessageSlaveHandler");
		try {
			if(msg instanceof OHUMessageSlave)
			{
				OHUMessageSlave slave = (OHUMessageSlave)msg;
				log.debug("PETE# Slave Message: " + slave.toString());
				msg.release();
				slave.release();
			}
		} catch (Exception e) {
			log.error("Error Slave Message:", e);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("Error. OHUSlaveHandler: ",cause);
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
