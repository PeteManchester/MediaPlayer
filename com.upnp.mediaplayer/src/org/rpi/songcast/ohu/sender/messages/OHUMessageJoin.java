package org.rpi.songcast.ohu.sender.messages;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.log4j.Logger;
import org.rpi.songcast.common.SongcastMessage;

import io.netty.buffer.ByteBuf;


public class OHUMessageJoin extends SongcastMessage {
	
	private Logger log = Logger.getLogger(this.getClass());
	private InetSocketAddress address = null;

	
	public OHUMessageJoin(ByteBuf buf, InetSocketAddress address) {
		super.setData(buf.retain());
		this.setAddress(address);
		log.debug("Join Message Received from Address: " + address);
		/*
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
		*/
	}

	
	@Override
	public String toString()
	{
		return "OHUMessageSlave";
	}


	/**
	 * @return the address
	 */
	public InetSocketAddress getAddress() {
		return address;
	}


	/**
	 * @param address the address to set
	 */
	private void setAddress(InetSocketAddress address) {
		this.address = address;
	}
}
