package org.rpi.songcast.ohu.sender.mpd;

import org.rpi.songcast.ohu.sender.response.OHUSenderAudioResponse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MPDStreamerMessageHandler extends SimpleChannelInboundHandler<ByteBuf> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		ByteBuf be = convertLEtoBE(msg);
		OHUSenderAudioResponse a = new OHUSenderAudioResponse(be);
		MPDStreamerController.getInstance().addSoundByte(a);		
	}
	
	/***
	 * Convert a Little Endian Byte Array to a Big Endian Byte Array
	 * @param in
	 * @return
	 */
	private ByteBuf convertLEtoBE(ByteBuf in) {
		ByteBuf out = Unpooled.buffer(in.readableBytes());
		while(in.readableBytes() >= 8) {
			double i = in.readDoubleLE();
			out.writeDouble(i);
		}
		
		return out;
	}

}
