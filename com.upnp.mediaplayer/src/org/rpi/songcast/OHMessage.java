package org.rpi.songcast;

import java.math.BigInteger;

import org.apache.log4j.Logger;
import org.rpi.songcast.events.EventOHZIURI;

public class OHMessage extends SongcastMessage {

	private Logger log = Logger.getLogger(this.getClass());

	public void checkMessageType() {
		byte[] protocol = getBytes(0, 3);
		// log.debug(byteToString(protocol));
		String sProtocol = byteToString(protocol);
		byte[] type = getBytes(5, 5);
		StringBuilder sb = new StringBuilder();
		for (byte b : type) {
			sb.append(String.format("%02X ", b));
		}
		String s = sb.toString().trim();
		if (s.equalsIgnoreCase("00")) {
			log.debug("URL Requst");
		} else if (s.equalsIgnoreCase("01")) {
			log.debug("URL Response");
			byte[] bHeader = getBytes(0, 7);
			try {
				String header = new String(bHeader, "UTF-8");
				log.debug("Header: " + header);
				byte[] zl = getBytes(7, 11);
				int zoneLength = new BigInteger(zl).intValue();
				byte[] zone = getBytes(16, (16 + zoneLength) - 1);
				String zoneName = new String(zone, "UTF-8");
				byte[] uriLength = getBytes(12, 15);
				int uriL = new BigInteger(uriLength).intValue();
				byte[] urlb = getBytes(16 + zoneLength, (16 + zoneLength + uriL) - 1);
				String uri = new String(urlb, "UTF-8");
				log.debug("Zone Name: " + zoneName + " URL: " + uri);
				EventOHZIURI ev = new EventOHZIURI();
				ev.setUri(uri);
				ev.setZone(zoneName);
				fireEvent(ev);

			} catch (Exception e) {
				log.error("Error Message Type 1", e);
			}
		} else if (s.equalsIgnoreCase("03")) {
			if (sProtocol.trim().toUpperCase().startsWith("OHM")) {
				log.debug("Audio Stream");
			}
		} else {
			log.debug("MesageType = " + s);
		}
	}

}
