package org.rpi.songcast.ohz.sender;

import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.rpi.songcast.common.SongcastMessage;

import io.netty.buffer.ByteBuf;

/***
 * Holder class for a ZoneQuery from a Receiver when this player is acting as a Sender.
 * The sender should respond with a single Zone URI message.
 * @author phoyle
 *
 */

public class OHZZoneQueryMessage extends SongcastMessage  {
	
	private Logger log = Logger.getLogger(this.getClass());	
	int zoneLength = 0;
	private String zoneId = "";
	
	public OHZZoneQueryMessage(ByteBuf buf) {
		super.setData(buf.retain());
		int zl = buf.getInt(8);
		String buffs = buf.toString(Charset.defaultCharset());
		int start_point = 12;
		String zoneId = buffs.substring(start_point, zl + start_point);
		setZoneLength(zl);
		setZoneId(zoneId);
		log.debug(this.toString());
	}

	/**
	 * @return the zoneLength
	 */
	public int getZoneLength() {
		return zoneLength;
	}
	/**
	 * @param zoneLength the zoneLength to set
	 */
	public void setZoneLength(int zoneLength) {
		this.zoneLength = zoneLength;
	}
	
	/**
	 * @return the zoneId
	 */
	public String getZoneId() {
		return zoneId;
	}
	/**
	 * @param zoneId the zoneId to set
	 */
	public void setZoneId(String zoneId) {
		this.zoneId = zoneId;
	}
	

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OHZZoneQueryMessage [zoneLength=");
		builder.append(zoneLength);
		builder.append(", zoneId=");
		builder.append(getZoneId());
		builder.append("]");
		return builder.toString();
	}


}
