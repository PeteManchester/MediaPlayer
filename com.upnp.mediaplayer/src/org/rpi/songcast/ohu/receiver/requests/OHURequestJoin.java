package org.rpi.songcast.ohu.receiver.requests;

/*
 * Join the OHM stream to start receiving audio stream.
 */

//Offset    Bytes                   Desc
//0         4                       "Ohz "
//4         1                       OhzHeader Major Version 1
//5         1                       Msg Type (0 = Query, 1 = Uri)
//6         2                       Total Bytes (Absolutely all bytes in the entire frame)

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import org.apache.log4j.Logger;

public class OHURequestJoin {
	
	private String header = "Ohm ";	
	private Logger log = Logger.getLogger(this.getClass());
	private ByteBuf buffer = null;
	
	public OHURequestJoin()
	{
		byte[] version = new byte[] { (byte) (1 & 0xff) };
		byte[] type = new byte[] { (byte) (0 & 0xff) };
		int length = header.length() + 1 + 1 + 2;
		ByteBuf test = Unpooled.buffer(length);
		test.setBytes(0, header.getBytes(CharsetUtil.UTF_8));
		test.setBytes(4, version);
		test.setBytes(5, type);
		test.setShort(6, length);
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
