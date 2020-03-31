package org.rpi.songcast.ohu.sender.mpd;

import java.util.List;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

@Deprecated
public class MPDStreamerBuffer extends ByteToMessageDecoder {
	Logger log = Logger.getLogger(this.getClass());
	private boolean isFirstTime = true;
	private int iCount = 0;
	private int maxSize = 0;

	public MPDStreamerBuffer() {
		log.debug("Creating MPDStreamerBuffer");
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
		
		
		int i = in.readableBytes();
		if (i < 1764) {
			log.debug("ReadableBytes was less than 1764: " + i);
			return;
		}
		
		if(i > maxSize) {
			maxSize = i;
		}
		
		if(iCount % 1000 == 0) {
			log.debug("MPDStreamerBuffer " + iCount + " Largest Buffer: " + maxSize);
			maxSize = 0;
		}
		iCount++;
		
		out.add(in.readBytes(1764));
		
		//while(in.readableBytes()> 1764) {
		//	out.add(in.readBytes(1764));
		//}
		
		
		
		//log.debug("ReadableBytes: " + i);
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("Error. MPDStreamerBuffer Error: " + cause);
		ctx.close();
	}

}
