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
		ByteBuf test = Unpooled.buffer(length);
		test.setBytes(0, header.getBytes(CharsetUtil.UTF_8));
		test.setBytes(4, version);
		test.setBytes(5, type);
		test.setShort(6, length);
		test.setInt(8, length_zone);
		test.setInt(12, length_url);
		test.setBytes(16, zone.getBytes(CharsetUtil.UTF_8));
		test.setBytes(16 +length_zone, url.getBytes(CharsetUtil.UTF_8));		
		buffer = Unpooled.copiedBuffer(test.array());
		test.release();
		log.debug(buffer.toString(CharsetUtil.UTF_8));
	}
	
	/**
	 * @return the buffer
	 */
	public ByteBuf getBuffer() {
		return buffer;
	}

}
