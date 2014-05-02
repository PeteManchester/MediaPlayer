package org.rpi.netty.songcast.ohu;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Basic OHUMessage
 * @author phoyle
 *
 */

public class OHUMessage {
	
	private ByteBuf data = null;

	/**
	 * @return the data
	 */
	public ByteBuf getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(ByteBuf data) {
//		byte[] bytes = new byte[data.readableBytes()];
//		data.getBytes(0, data);
//		ByteBuf buf = Unpooled.copiedBuffer(bytes);
//		this.data = buf;
		this.data = data.copy();
	}

}
