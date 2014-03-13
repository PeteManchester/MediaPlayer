package org.rpi.songcast.ohm;

import org.apache.log4j.Logger;
import org.rpi.songcast.core.SongcastMessage;

/*
 * Join the OHM stream to start receiving audio stream.
 */

//Offset    Bytes                   Desc
//0         4                       "Ohz "
//4         1                       OhzHeader Major Version 1
//5         1                       Msg Type (0 = Query, 1 = Uri)
//6         2                       Total Bytes (Absolutely all bytes in the entire frame)


public class OHMRequestListen extends SongcastMessage {
	
private Logger log = Logger.getLogger(this.getClass());
	
	private String header = "4f686d200101";

	public  OHMRequestListen(String zone)
	{
		String zoneHex = stringToHex(zone);
		//log.debug("Zone in HEX: " + zoneHex);
		String lengthPacket = "0000";
		int length = header.length() + lengthPacket.length();
		length = length/2;
		lengthPacket = DecToHex(length, 4);
		data = hexStringToByteArray(header+ lengthPacket);
	}
	
	@Override
	public String toString()
	{
		return this.getClass().getName() +  " : " + header;
	}

}