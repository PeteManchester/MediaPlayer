package org.rpi.netty.songcast.ohz;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.log4j.Logger;

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
			case 0:// Zone Query
				break;
			case 1:// Zone URI
				OHZMessage message = new OHZMessage();
				int zl = buf.getInt(8);
				int uri_length = buf.getInt(12);
				String buffs = buf.toString(Charset.defaultCharset());
				int start_point = 16;
				String zone = buffs.substring(start_point, zl + start_point);
				String uri = buffs.substring(start_point + zl, uri_length + start_point + zl);
				message.setZone(zone);
				message.setUri(uri);
				log.debug(message.toString());
				out.add(message);
				break;
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
