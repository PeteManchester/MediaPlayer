package org.rpi.songcast.ohu.sender.response;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class OHUSenderTrackResponse {
	
	private String header = "Ohm ";
	private Logger log = Logger.getLogger(this.getClass());
	private ByteBuf buffer = null;
	private String metadata = "";
	private String url = "";
	private int sequence = 0;
	
	public OHUSenderTrackResponse(int sequeunce, String url, String metadata) {
		this.metadata = metadata;
		this.url = url;
		this.sequence = sequeunce;
		
		//byte[] version = new byte[] { (byte) (1 & 0xff) };
		//byte[] type = new byte[] { (byte) (4 & 0xff) };

		int length = header.length() + 1 + 1 + 2 + 4 + 4 + 4 + url.length() + metadata.length() ;
		buffer = Unpooled.directBuffer(length);
		//ByteBuf test = Unpooled.buffer(length);
		buffer.writeBytes(header.getBytes(CharsetUtil.UTF_8));
		buffer.writeByte( 1);//Version
		buffer.writeByte( 4);//Type
		buffer.writeShort( length);
		buffer.writeInt( sequeunce);
		buffer.writeInt( url.length());
		buffer.writeInt( metadata.length());	
		buffer.writeBytes(Unpooled.copiedBuffer(url, CharsetUtil.UTF_8));
		buffer.writeBytes( Unpooled.copiedBuffer(metadata, CharsetUtil.UTF_8));
		//log.debug("ReadableBytes: " + buffer.readableBytes());
		//buffer = Unpooled.copiedBuffer(test.array());		
		//OHUMessageTrack testTrack = new OHUMessageTrack(buffer.retain());
		
		//log.debug("ReadableBytes: " + testTrack.toString());
	}
	
	/**
	 * @return the buffer
	 */
	public ByteBuf getBuffer() {
		return buffer;
	}

}
