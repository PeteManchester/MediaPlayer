package org.rpi.songcast.ohu;

/**
 * Get the Slave Endpoint information
 */


//Offset    Bytes                   Desc
//0         4                       Slave count (n)
//4         6 * n                   Slave address/port list


import io.netty.buffer.ByteBuf;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class OHUMessageSlave extends OHUMessage {
	
	private Logger log = Logger.getLogger(this.getClass());
	private ConcurrentHashMap<String, Slave> endpoints = new ConcurrentHashMap<String, Slave>();

	
	public OHUMessageSlave(ByteBuf buf) {
		super.setData(buf.retain());
		int slave_count = buf.getInt(8);
		log.debug("Slave Count: " + slave_count);		
		for (int i = 0; i < slave_count; i++) {
			byte[] endpoint = new byte[4]; 
			int start = 12 + (6*i);
			buf.getBytes(start, endpoint,0,4);
			try {				
				InetAddress address = InetAddress.getByAddress(endpoint);
				int port = buf.getUnsignedShort(start+4);
				Slave sl = new Slave(address,port);

				log.debug("Adding Slave Endpoint: " + sl.toString());
				getEndpoints().put(sl.getName(), sl);
			} catch (UnknownHostException e) {
				log.error("Slave Message Error",e);
			}
		}
	}

	/**
	 * @return the endpoints
	 */
	public ConcurrentHashMap<String, Slave> getEndpoints() {
		return endpoints;
	}
	
	@Override
	public String toString()
	{
		return "OHUMessageSlave";
	}
}
