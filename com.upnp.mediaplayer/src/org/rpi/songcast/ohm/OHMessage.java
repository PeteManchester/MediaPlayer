package org.rpi.songcast.ohm;

import org.apache.log4j.Logger;
import org.rpi.songcast.core.SongcastManager;
import org.rpi.songcast.core.SongcastMessage;
public class OHMessage extends SongcastMessage {

	private Logger log = Logger.getLogger(this.getClass());
	
	public void checkMessageType() {
		checkMessageType(null);
	}

	public void checkMessageType(SongcastManager scManager) {
		byte[] protocol = getBytes(0, 3);
		// Get the Protocol
		String sProtocol = byteToString(protocol);
		//byte[] type = getBytes(5, 5);
		//int iType = new BigInteger(type).intValue();
		 if (sProtocol.toUpperCase().startsWith("OHM")) {
			 //Put OHM messages into a seperate thread so we don't block the receiver..
			 scManager.putMessage(data);
		} else if (sProtocol.toUpperCase().startsWith("OHZ")) {
			scManager.putMessage(data);
		}
	}
}
