package org.rpi.songcast.ohz.common;

import java.util.List;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohz.sender.OHZZoneQueryMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

public class OHZMessageDecoder extends MessageToMessageDecoder<DatagramPacket> {

	private Logger log = Logger.getLogger(this.getClass());

	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
		if (msg instanceof DatagramPacket) {

			DatagramPacket packet = (DatagramPacket) msg;
			ByteBuf buf = packet.content();
			int type = buf.getByte(5) & ~0x80;
			switch (type) {
			case 0:// Zone Query Used by Receiver to start playing
			{
				
				log.info("Zone Query: " + buf.toString());				
				OHZZoneQueryMessage  ohz = new OHZZoneQueryMessage(buf);				
				out.add(ohz);				
				break;
			}

			case 1:// Zone URI Sent by Sender in response to a ZoneQuery
			{
				OHZZoneUriMessage message = new OHZZoneUriMessage(buf);
				out.add(message);
				break;
			}
			case 2:// Preset Query
				break;
			case 3:// Preset Info
				break;
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error(cause);
		ctx.close();
	}

}
