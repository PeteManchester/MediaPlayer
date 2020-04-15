package org.rpi.songcast.ohu.sender.response;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class OHUSenderMetaTextResponse {
	
	private String header = "Ohm ";
	private Logger log = Logger.getLogger(this.getClass());
	private ByteBuf buffer = null;
	private String metatext = "";
	private int sequence = 0;
	
	public OHUSenderMetaTextResponse(int sequence, String metatext) {
		this.metatext = metatext;
		this.sequence = sequence;
		
		//byte[] version = new byte[] { (byte) (1 & 0xff) };
		//byte[] type = new byte[] { (byte) (5 & 0xff) };

		int length = header.length() + 1 + 1 + 2 + 4 + 4  +  metatext.length() ;
		buffer = Unpooled.buffer(length);
		//ByteBuf test = Unpooled.buffer(length);
		buffer.writeBytes(header.getBytes(CharsetUtil.UTF_8));
		buffer.writeByte( 1);//Version
		buffer.writeByte( 5);//Type
		buffer.writeShort( length);
		buffer.writeInt( sequence);
		buffer.writeInt( metatext.length());
		buffer.writeBytes( Unpooled.copiedBuffer(metatext, CharsetUtil.UTF_8));

		log.debug(this.toString());
		//log.debug("ReadableBytes: " + testTrack.toString());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OHUSenderMetaTextResponse [sequence=");
		builder.append(sequence);
		builder.append(", metatext=");
		builder.append(metatext);
		builder.append("]");
		return builder.toString();
	}
	
	public ByteBuf getBuffer() {
		return buffer;
	}

}
