package org.rpi.songcast.ohz.sender;

import org.apache.log4j.Logger;
import org.rpi.config.Config;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;


public class OHZZoneUriResponse {
	
	private String header = "Ohm ";	
	private Logger log = Logger.getLogger(this.getClass());
	private ByteBuf buffer = null;
	
	public OHZZoneUriResponse(String myURI, String zone)
	{
		byte[] version = new byte[] { (byte) (1 & 0xff) };
		byte[] type = new byte[] { (byte) (1 & 0xff) };
		//String zone = Config.getInstance().getMediaplayerFriendlyName();
		int length_zone = zone.length();
		String url = "ohu://" + myURI;
		int length_url = url.length();
		int length = header.length() + 1 + 1 + 2 + 4 + 4 + length_zone + length_url;
		//ByteBuf test = Unpooled.buffer(length);
		buffer = Unpooled.directBuffer(length);
		buffer.writeBytes( header.getBytes(CharsetUtil.UTF_8));
		buffer.writeBytes( version);
		buffer.writeBytes( type);
		buffer.writeShort( length);
		buffer.writeInt( length_zone);
		buffer.writeInt( length_url);
		buffer.writeBytes( zone.getBytes(CharsetUtil.UTF_8));
		buffer.writeBytes( url.getBytes(CharsetUtil.UTF_8));		
		//buffer = Unpooled.copiedBuffer(test.array());
		//test.release();
		log.debug(buffer.toString(CharsetUtil.UTF_8));
	}
	
	/**
	 * @return the buffer
	 */
	public ByteBuf getBuffer() {
		return buffer;
	}

}
