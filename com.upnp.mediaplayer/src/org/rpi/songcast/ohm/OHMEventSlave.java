package org.rpi.songcast.ohm;


import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.rpi.songcast.core.SlaveEndpoint;
import org.rpi.songcast.core.SongcastMessage;

//Offset    Bytes                   Desc
//0         4                       Slave count (n)
//4         6 * n                   Slave address/port list

public class OHMEventSlave extends SongcastMessage {

	private Logger log = Logger.getLogger(this.getClass());
	private ConcurrentHashMap<String, SlaveEndpoint> endpoints = new ConcurrentHashMap<String, SlaveEndpoint>();

	public void checkMessageType(OHMMessageQueue mq) {
		
//		StringBuilder sb = new StringBuilder();
//		for (byte b : data) {
//			sb.append(String.format("%02X ", b));
//		}
//		String s = sb.toString();
//		log.debug("Slave Message: " + s);

		int slave_count = new BigInteger(getBytes(8, 11)).intValue();
		byte[] slave = getBytes(12, 17);
		for (int i = 0; i < slave_count; i++) {
			byte[] endpoint = getBytes(12+(6*i), 17+(6*1));
			try {				
				InetAddress address = InetAddress.getByAddress(getBytesFragment(0, 3, endpoint));
				byte[] bPort = getBytesFragment(4, 5, endpoint);
				int port = ((bPort[0] << 8) & 0x0000ff00) | (bPort[1] & 0x000000ff);
				SlaveEndpoint sl = new SlaveEndpoint(address,port);
				log.debug("Adding Slave Endpoint: " + sl.toString());
				endpoints.put(sl.getName(), sl);
			} catch (UnknownHostException e) {
				log.error("Slave Message Error",e);
			}
		}
		
		List<String> delete = new ArrayList<String>();
		for(SlaveEndpoint slep : mq.getEndpoints().values())
		{
			if(!endpoints.containsKey(slep.getName()))
			{
				delete.add(slep.getName());
			}
		}
		
		mq.removeEndpoint(delete);
		
		for(SlaveEndpoint slep : endpoints.values())
		{
			mq.addEndpoint(slep);
		}
	}
	
	private byte[] getBytesFragment(int start, int end,byte[] bytes)
	{
		int size = (end - start) + 1;
		int count = 0;
		byte[] res = new byte[size];
		for(int i = start;i<=end;i++)
		{
			res[count] = bytes[i];
			count++;
		}
		return res;
	}

	public ConcurrentHashMap<String, SlaveEndpoint> getEndpoints() {
		// TODO Auto-generated method stub
		return endpoints;
	}
}
