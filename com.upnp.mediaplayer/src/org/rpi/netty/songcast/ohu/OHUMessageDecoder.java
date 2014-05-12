package org.rpi.netty.songcast.ohu;

/**
 * Create a OHUMesage from the ByteBuf
 * Which is then handled by the handlers in the pipeline
 */

import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

public class OHUMessageDecoder extends MessageToMessageDecoder<DatagramPacket> {

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
				System.out.println("Unknown Message: " + buf.toString(Charset.forName("utf-8")));
				//message = new OHUMessage();
				//message.setData(buf.retain());
				break;
			}
			
		}
	}
}
