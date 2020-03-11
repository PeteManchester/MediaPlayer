package org.rpi.songcast.ohu.receiver.messages;

import io.netty.buffer.ByteBuf;

//Offset    Bytes                   Desc
//0         4                       Sequence (n)
//4         4                       Metatext Bytes (n)
//8         n                       n bytes of metatext

import org.apache.log4j.Logger;
import org.rpi.songcast.common.SongcastMessage;

public class OHUMessageMetaText extends SongcastMessage {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private int sequence = -99;
	private String metaText = "";

	public OHUMessageMetaText(ByteBuf buf) {
		super.setData(buf.retain());
		sequence = buf.getInt(8);
		int meta_length = buf.getInt(12);
		if (meta_length > 0) {
			byte[] bmeta_data = new byte[meta_length];
			buf.getBytes(16, bmeta_data,0,meta_length);
			metaText = new String(bmeta_data);
			log.debug(this.toString());
		}
	}

	/**
	 * @return the metaText
	 */
	public String getMetaText() {
		return metaText;
	}

	/**
	 * @return the sequence
	 */
	public int getSequence() {
		return sequence;
	}	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("OHUMessageMetaText");
		sb.append("\r\n");
		sb.append("Sequence: " + getSequence());
		sb.append("\r\n");
		sb.append("MetaText: " + getMetaText());
		return sb.toString();
	}

}
