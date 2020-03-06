package org.rpi.songcast.ohz.common;

import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.rpi.songcast.common.SongcastMessage;

import io.netty.buffer.ByteBuf;

/***
 * This is a zone URI message from the sender giving details of the URL it should use to get establish audio connection
 * @author phoyle
 *
 */

public class OHZZoneUriMessage extends SongcastMessage {
	
	//private Logger log = Logger.getLogger(this.getClass());	
	
	private String zone = "";
	private String uri = "";
	
	
	public OHZZoneUriMessage(ByteBuf buf) {
		int zl = buf.getInt(8);
		int uri_length = buf.getInt(12);
		String buffs = buf.toString(Charset.defaultCharset());
		int start_point = 16;
		String zone = buffs.substring(start_point, zl + start_point);
		String uri = buffs.substring(start_point + zl, uri_length + start_point + zl);
		setZone(zone);
		setUri(uri);
		//log.debug(this.toString());
	}
	
	/**
	 * @return the zone
	 */
	public String getZone() {
		return zone;
	}
	/**
	 * @param zone the zone to set
	 */
	public void setZone(String zone) {
		this.zone = zone;
	}
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("OHZMessage");
		sb.append("\r\n");
		sb.append("URI: " + uri);
		sb.append("\r\n");
		sb.append("Zone: " + zone);
		return sb.toString();
	}
}
