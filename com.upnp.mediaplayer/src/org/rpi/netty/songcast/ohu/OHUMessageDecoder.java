package org.rpi.netty.songcast.ohu;

/**
 * Create a OHUMesage from the ByteBuf
 * Which is then handled by the handlers in the pipeline
 */

import java.nio.charset.Charset;
import java.util.List;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

public class OHUMessageDecoder extends MessageToMessageDecoder<DatagramPacket> {
	
	private Logger log = Logger.getLogger(this.getClass());

	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
		if (msg instanceof DatagramPacket) {
			ByteBuf buf = msg.content();
			int type = buf.getByte(5) & ~0x80;
			OHUMessage message = null;
			switch (type) {
			case 3:// Audio
				message = new OHUMessageAudio(buf);
				out.add(message);
				break;
			case 4:// Track
				message = new OHUMessageTrack(buf);
				out.add(message);
				break;
			case 5:// MetaText
				message = new OHUMessageMetaText(buf);
				out.add(message);
				break;
			case 6:// Slave
				message = new OHUMessageSlave(buf);
				out.add(message);
				break;
			default:
				log.info("Unknown Message: " + buf.toString(Charset.forName("utf-8")));
				break;
			}
			
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error(cause);
		//ctx.close();
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
