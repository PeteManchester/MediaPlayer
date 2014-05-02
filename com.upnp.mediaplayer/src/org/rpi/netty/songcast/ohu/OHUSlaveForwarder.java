package org.rpi.netty.songcast.ohu;

/**
 * Handle the OHUMessages
 * Forward all messages except Slave messages to any Slave Endpoints
 * Then pass on down the pipeline
 */

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class OHUSlaveForwarder extends SimpleChannelInboundHandler<OHUMessage> {

	private Logger log = Logger.getLogger(this.getClass());
	private ConcurrentHashMap<String, Slave> endpoints = new ConcurrentHashMap<String, Slave>();

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, OHUMessage msg) throws Exception {
		if (msg instanceof OHUMessageSlave) {
			OHUMessageSlave slave = (OHUMessageSlave) msg;
			endpoints = slave.getEndpoints();
		} else {
			for (Slave sl : endpoints.values()) {
				try {
					byte[] data = new byte[msg.getData().readableBytes()];
					msg.getData().getBytes(0, data,0,msg.getData().readableBytes());
					InetSocketAddress to = sl.getRemoteAddress();
					DatagramPacket packet = new DatagramPacket(msg.getData(), to);
					ctx.channel().writeAndFlush(packet).sync();
				} catch (Exception e) {
					log.error("Error forwarding to SlaveEndpoint", e);
				}
			}
		}
		ctx.fireChannelRead(msg);
	}

}
