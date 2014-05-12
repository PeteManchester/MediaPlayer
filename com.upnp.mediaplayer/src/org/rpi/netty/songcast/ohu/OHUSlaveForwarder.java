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
import org.scratchpad.songcast.ohm.OHMEventTrack;

public class OHUSlaveForwarder extends SimpleChannelInboundHandler<OHUMessage> {

	private Logger log = Logger.getLogger(this.getClass());
	private ConcurrentHashMap<String, Slave> endpoints = new ConcurrentHashMap<String, Slave>();

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, OHUMessage msg) throws Exception {
		if (msg instanceof OHUMessageSlave) {
			// Don't send Slave messages to the Slaves..
			OHUMessageSlave slave = (OHUMessageSlave) msg;
			endpoints = slave.getEndpoints();
			// msg.getData().release();
		} else {
			if (endpoints.size() > 0) {
				for (Slave sl : endpoints.values()) {
					try {
						InetSocketAddress toAddress = sl.getRemoteAddress();
						DatagramPacket packet = new DatagramPacket(msg.getData().retain(), toAddress);
						ctx.channel().writeAndFlush(packet);
					} catch (Exception e) {
						log.error("Error forwarding to SlaveEndpoint", e);
					}
				}
			}
		}
		try {
			msg.getData().release();
		} catch (Exception e) {
			log.error("Error Releasing Data", e);
		}
		ctx.fireChannelRead(msg);
	}

}
