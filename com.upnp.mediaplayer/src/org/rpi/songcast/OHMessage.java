package org.rpi.songcast;

import java.math.BigInteger;

import org.apache.log4j.Logger;
import org.rpi.songcast.events.EventOHZIURI;

public class OHMessage extends SongcastMessage {

	private Logger log = Logger.getLogger(this.getClass());

	public void checkMessageType() {
		byte[] protocol = getBytes(0, 3);
		// Get the Protocol
		String sProtocol = byteToString(protocol);
		byte[] type = getBytes(5, 5);

		int iType = new BigInteger(type).intValue();

		 if (sProtocol.toUpperCase().startsWith("OHM")) {
			switch (iType) {
			case 3:
				OHMResponseJoin res = new OHMResponseJoin();
				res.data = data;
				res.checkMessageType();
				break;
			default:
				log.debug("OHM Message: " + iType);
			}

		} else if (sProtocol.toUpperCase().startsWith("OHZ")) {
			switch (iType) {
			case 0:
				log.debug("URL Requst");
				break;
			case 1:
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
				break;
			default:
				log.debug("OHZ Message: " + iType);
			}
		}
	}
}
