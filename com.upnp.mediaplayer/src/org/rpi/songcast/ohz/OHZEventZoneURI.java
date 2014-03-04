package org.rpi.songcast.ohz;

import java.math.BigInteger;

import org.apache.log4j.Logger;
import org.rpi.songcast.core.SongcastMessage;
import org.rpi.utils.Utils;

public class OHZEventZoneURI extends SongcastMessage {

	Logger log = Logger.getLogger(this.getClass());
	
	private String uri = "";
	private String zoneName = "";

	public void checkMessageType() {
		try {
			byte[] zl = getBytes(7, 11);
			int zoneLength = new BigInteger(zl).intValue();
			byte[] zone = getBytes(16, (16 + zoneLength) - 1);
			zoneName = new String(zone, "UTF-8");
			byte[] uriLength = getBytes(12, 15);
			int uriL = new BigInteger(uriLength).intValue();
			byte[] urlb = getBytes(16 + zoneLength, (16 + zoneLength + uriL) - 1);
			uri = new String(urlb, "UTF-8");
			log.debug("OHZ Respsone (1) URI. Zone Name: " + zoneName + " URL: " + uri);
		} catch (Exception e) {
			log.error("Error getting ZoneURI", e);
		}
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
	private void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the zoneName
	 */
	public String getZoneName() {
		return zoneName;
	}

	/**
	 * @param zoneName the zoneName to set
	 */
	private void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}
	
	public boolean isOKToConnect()
	{
		if((!Utils.isEmpty(uri)) && (!Utils.isEmpty(zoneName)))
		{
			return true;
		}
		return false;
	}

}
