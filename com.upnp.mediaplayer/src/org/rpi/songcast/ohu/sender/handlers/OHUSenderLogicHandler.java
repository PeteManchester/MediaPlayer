package org.rpi.songcast.ohu.sender.handlers;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.rpi.config.Config;
import org.rpi.songcast.ohu.sender.OHUSenderConnection;
import org.rpi.songcast.ohu.sender.messages.OHUMessageJoin;
import org.rpi.songcast.ohu.sender.messages.OHUMessageLeave;
import org.rpi.songcast.ohz.common.OHZZoneQueryRequest;
import org.rpi.songcast.ohz.sender.OHZZoneQueryMessage;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class OHUSenderLogicHandler extends SimpleChannelInboundHandler<Object> {

	private Logger log = Logger.getLogger(this.getClass());
	private String latestZone = "";
	private InetAddress localInetAddr = null;
	private InetSocketAddress remoteInetSocketAddr = null;
	private InetSocketAddress localInetSocketAddr = null;
	private long lastZoneQueryRequest = 0;
	private OHUSenderConnection ohuSender = null;
	private String myZoneId = Config.getInstance().getMediaplayerFriendlyName();
	
	private boolean isConnected = false;

	/*
	public OHUSenderLogicHandler(InetAddress localInetAddr, InetSocketAddress remoteInetSocketAddr, InetSocketAddress localInetSocketAddress) {
		this.localInetAddr = localInetAddr;
		this.remoteInetSocketAddr = remoteInetSocketAddr;
		this.localInetSocketAddr = localInetSocketAddress;
	}
	*/

	/*
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		log.debug("write..");
		if (msg instanceof OHZZoneQueryRequest) {
			OHZZoneQueryRequest ohzZR = (OHZZoneQueryRequest) msg;
			DatagramPacket packet = new DatagramPacket(ohzZR.getBuffer(), remoteInetSocketAddr, localInetSocketAddr);
			lastZoneQueryRequest = System.currentTimeMillis();
			ctx.write(packet, promise);
		} else {
			ctx.write(msg, promise);
		}

	}
	*/

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//log.debug("Read");

		

		// ctx.fireChannelRead(msg);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof OHUMessageJoin) {
			//long timeNow = System.currentTimeMillis();
			isConnected = true;
		} else if (msg instanceof OHUMessageLeave) {
			//OHUMessageLeave ohz = (OHUMessageLeave) msg;
			isConnected = false;			
		}
	}
}
