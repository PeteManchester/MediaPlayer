package org.rpi.songcast.ohz;

import org.apache.log4j.Logger;
import org.rpi.songcast.core.SongcastMessage;

//Offset    Bytes                   Desc
//0         4                       "Ohz "
//4         1                       OhzHeader Major Version 1
//5         1                       Msg Type (0 = Query, 1 = Uri)
//6         2                       Total Bytes (Absolutely all bytes in the entire frame)
//8         4                       Length of Zone n
//12        n                       Zone

public class OHZRequestJoin extends SongcastMessage {

	private Logger log = Logger.getLogger(this.getClass());

	private String header = "4f687a200100";


	public  OHZRequestJoin(String zone)
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
		data = hexStringToByteArray(header+ lengthPacket +  sZL + zoneHex);
	}
	
	@Override
	public String toString()
	{
		return this.getClass().getName() +  " : " + header;
	}

}
