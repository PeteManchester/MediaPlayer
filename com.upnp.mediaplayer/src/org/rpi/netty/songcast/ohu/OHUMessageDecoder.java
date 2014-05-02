package org.rpi.netty.songcast.ohu;

/**
 * Create a OHUMesage from the ByteBuf
 * Which is then handled by the handlers in the pipeline
 */

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

public class OHUMessageDecoder extends MessageToMessageDecoder<DatagramPacket> {

	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
		if(msg instanceof DatagramPacket)
		{
			ByteBuf buff = msg.content();
			OHUMessage mess = OHUMessageFactory.buildMessage(buff);
			out.add(mess);
		}		
	}

}
