package org.rpi.songcast.ohz.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import org.apache.log4j.Logger;
import org.rpi.songcast.common.SongcastMessage;

//Offset    Bytes                   Desc
//0         4                       "Ohm "
//4         1                       OhzHeader Major Version 1
//5         1                       Msg Type (02 Leave)
//6         2                       Total Bytes (Absolutely all bytes in the entire frame)

public class OHZLeaveRequest extends SongcastMessage {

	private String header = "Ohm ";
	private Logger log = Logger.getLogger(this.getClass());
	private ByteBuf buffer = null;

	public OHZLeaveRequest() {
		//byte[] version = new byte[] { (byte) (1 & 0xff) };
		//byte[] type = new byte[] { (byte) (2 & 0xff) };
		int length = header.length() + 1 + 1 + 2;
		buffer = Unpooled.buffer(length);
		buffer.writeBytes(header.getBytes(CharsetUtil.UTF_8));
		buffer.writeByte(1);//Version
		buffer.writeByte(2);//Type
		buffer.writeShort(length);
		// buffer = Unpooled.copiedBuffer(test.array());
		// test.release();
		log.debug(buffer.toString(CharsetUtil.UTF_8));
	}

	/**
	 * @return the buffer
	 */
	public ByteBuf getBuffer() {
		return buffer;
	}

}
