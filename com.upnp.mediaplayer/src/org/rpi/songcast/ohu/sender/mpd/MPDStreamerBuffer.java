package org.rpi.songcast.ohu.sender.mpd;

import java.util.List;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MPDStreamerBuffer extends ByteToMessageDecoder {
	Logger log = Logger.getLogger(this.getClass());
	private boolean isFirstTime = true;

	public MPDStreamerBuffer() {
		log.debug("This is the First Time");
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		//log.debug("Decode");
		if (in.readableBytes() < 1764) {
			return;
		}
		
		if (isFirstTime) {
			int test = 160;
			byte[] bytes = new byte[test];
			in.retain().getBytes(0,bytes);
			
			String sCodec = "";
			try {
				sCodec = new String(bytes, "UTF-8");
				log.debug(sCodec);
			} catch (Exception e) {

			}
			
			isFirstTime = false;
		}
		
		if (in.readableBytes() < 1764) {
			return;
		}
		
		out.add(in.readBytes(1764));
	}

}
