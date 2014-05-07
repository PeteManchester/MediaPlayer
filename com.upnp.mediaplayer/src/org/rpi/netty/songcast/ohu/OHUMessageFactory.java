package org.rpi.netty.songcast.ohu;

/**
 * Factory to create our OHUMessage from the ByteBuf
 */

import java.nio.charset.Charset;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;

public class OHUMessageFactory {
	
	private static Logger log = Logger.getLogger("OHUMessageFactory");
	
	public static OHUMessage buildMessage(ByteBuf buf)
	{
		int type = buf.getByte(5) & ~0x80;	
		switch(type)
		{
		case 3://Audio
			OHUMessageAudio audio = new OHUMessageAudio(buf);
			return audio;
		case 4://Track
			log.debug("Track Message: " + buf.toString(Charset.defaultCharset()));
			OHUMessageTrack track = new OHUMessageTrack(buf);
			return track;
		case 5://MetaText
			log.debug("MetaText Message: " + buf.toString(Charset.defaultCharset()));
			OHUMessageMetaText text = new OHUMessageMetaText(buf);
			return text;
		case 6://Slave
			log.debug("Slave Message: " + buf.toString(Charset.defaultCharset()));
			OHUMessageSlave slave = new OHUMessageSlave(buf);
			return slave;		
		}
		return new OHUMessage();
	}
}
