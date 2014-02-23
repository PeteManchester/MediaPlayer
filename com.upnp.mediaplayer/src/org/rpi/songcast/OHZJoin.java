package org.rpi.songcast;

import org.apache.log4j.Logger;

public class OHZJoin extends SongcastMessage {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private String header = "4f687a200100";

	public  OHZJoin(String zone)
	{
		String zoneHex = stringToHex(zone);
		log.debug("Zone in HEX: " + zoneHex);
		String lengthPacket = "0000";
		int zl = zoneHex.length();
		String lengthZone = "00000000";
		int length = header.length() + lengthPacket.length() + lengthZone.length() + zoneHex.length();
		length = length/2;
		lengthPacket = DecToHex(length, 4);
		String sZL= DecToHex(zl/2, 8);
		//byte[] queryZone = hexStringToByteArray("6f687a200100"+ "0024" + "00000010" + "adb3ff3c41b7ebd669a49e35d54222ae");
		data = hexStringToByteArray(header+ lengthPacket +  sZL + zoneHex);
	}
	
}
