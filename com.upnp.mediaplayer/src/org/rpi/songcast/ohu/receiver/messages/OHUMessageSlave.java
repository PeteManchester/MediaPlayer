package org.rpi.songcast.ohu.receiver.messages;

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
import org.rpi.songcast.common.SongcastMessage;
import org.rpi.songcast.ohu.receiver.SlaveInfo;

public class OHUMessageSlave extends SongcastMessage {
	
	private Logger log = Logger.getLogger(this.getClass());
	private ConcurrentHashMap<String, SlaveInfo> endpoints = new ConcurrentHashMap<String, SlaveInfo>();

	
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
				SlaveInfo sl = new SlaveInfo(address,port);
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
	public ConcurrentHashMap<String, SlaveInfo> getEndpoints() {
		return endpoints;
	}
	
	@Override
	public void release() {		
		int refCount = data.refCnt();
		if(refCount>0)
		{
			data.release(refCount);
			log.debug("Slave Released: " + toString());
		}	
		else {
			log.debug("Slave Already Released" + toString());
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OHUMessageSlave [endpoints=");
		builder.append(endpoints);
		builder.append("]");
		return builder.toString();
	}
}
