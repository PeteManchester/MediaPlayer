package org.rpi.songcast.ohz.common;

//Offset    Bytes                   Desc
//0         4                       "Ohz "
//4         1                       OhzHeader Major Version 1
//5         1                       Msg Type (0 = Query, 1 = Uri)
//6         2                       Total Bytes (Absolutely all bytes in the entire frame)
//8         4                       Length of Zone n
//12        n                       Zone

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;

public class OHZZoneQueryRequest {
	// private String header = "4f687a200100";
	private String header = "Ohz ";
	//private Logger log = Logger.getLogger(this.getClass());
	private ByteBuf buffer = null;
	private String zone = "";

	public OHZZoneQueryRequest(String zone) {
		this.zone = zone;
		byte[] version = new byte[] { (byte) (1 & 0xff) };
		byte[] type = new byte[] { (byte) (0 & 0xff) };
		int length = header.length() + 1 + 1 + 2 + 4 + zone.length();
		buffer = Unpooled.directBuffer(length);
		buffer.writeBytes( header.getBytes(CharsetUtil.UTF_8));
		buffer.writeBytes( version);
		buffer.writeBytes( type);
		buffer.writeShort( length);
		buffer.writeInt( zone.length());
		buffer.writeBytes( zone.getBytes(CharsetUtil.UTF_8));
		//buffer = Unpooled.copiedBuffer(test.array());
		//test.release();
		//log.debug(buffer.toString(CharsetUtil.UTF_8));
	}

	/**
	 * @return the buffer
	 */
	public ByteBuf getBuffer() {
		return buffer;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OHZZoneQueryRequest [zone=");
		builder.append(zone);
		builder.append("]");
		return builder.toString();
	}
}
