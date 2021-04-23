package org.rpi.songcast.ohu.receiver.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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
import org.rpi.songcast.common.SongcastMessage;
import org.rpi.songcast.ohu.receiver.OHUChannelInitializer;
import org.rpi.songcast.ohu.receiver.SlaveInfo;
import org.rpi.songcast.ohu.receiver.messages.OHUMessageSlave;

public class OHUSlaveForwarder extends SimpleChannelInboundHandler<SongcastMessage> {

	private Logger log = Logger.getLogger(this.getClass());
	private ConcurrentHashMap<String, SlaveInfo> endpoints = new ConcurrentHashMap<String, SlaveInfo>();
	private OHUChannelInitializer initializer = null;

	public OHUSlaveForwarder(OHUChannelInitializer initializer) {
		this.initializer = initializer;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SongcastMessage msg) throws Exception {
		if (msg instanceof OHUMessageSlave) {
			// Don't send Slave messages to the Slaves..
			log.debug("Slave Message in OHUSlaveForwarder: " + msg );
			OHUMessageSlave slave = (OHUMessageSlave) msg;
			endpoints = slave.getEndpoints();
			initializer.setEndpoints(endpoints);
			slave.release();
			msg.release();
		} else {
			if (endpoints.size() > 0) {
				ByteBuf buf = Unpooled.copiedBuffer(msg.getData());
				for (SlaveInfo sl : endpoints.values()) {
					try {
						InetSocketAddress toAddress = sl.getRemoteAddress();
						DatagramPacket packet = new DatagramPacket(buf.retain(), toAddress);
						ctx.writeAndFlush(packet);
					} catch (Exception e) {
						log.error("Error forwarding to SlaveEndpoint", e);
					}
				}
				
				//try {
					//ctx.flush();
				//} catch (Exception e) {
				//	log.error("Error Flushing", e);
				//} finally {
					try {
						int i = buf.refCnt();
						if (i > 0) {
							buf.release(i);
						}
					} catch (Exception e) {
						log.error("Error Release Buffer", e);
					}
				//}
			}
		}
		ctx.fireChannelRead(msg);
	}

}
