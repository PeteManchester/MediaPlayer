package org.rpi.netty.songcast.ohz;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.rpi.netty.songcast.ohu.OHUConnector;

public class OHZRequestHandler extends SimpleChannelInboundHandler<OHZMessage> {
	private Logger log = Logger.getLogger(this.getClass());
	private String zone = "";
	private OHUConnector ohu = null;
	private InetAddress localInetAddr = null;
	
	public OHZRequestHandler(InetAddress localInetAddr)
	{
		this.localInetAddr = localInetAddr;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, OHZMessage msg) throws Exception {
		if (msg instanceof OHZMessage) {
			OHZMessage mess = (OHZMessage) msg;
			if (mess.getUri().endsWith("0.0.0.0:0")) {
				if (zone.equalsIgnoreCase(mess.getZone())) {
					log.debug("Stop OHUConnector");
					if (ohu != null) {
						ohu.stop();
						ohu = null;
						zone = "";
					}
				}
			} else if (ohu ==null) {
					zone = mess.getZone();
					InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
					ohu = new OHUConnector(mess.getUri(), mess.getZone(), localInetAddr);
					ohu.run();
			}			
		} 
		ctx.fireChannelRead(msg);
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
		if (ohu != null) {
			ohu.stop();
			ohu = null;
		}
		super.channelUnregistered(ctx);
	}

}
