package org.rpi.songcast.ohu.sender.messages;

import java.net.SocketAddress;

import org.apache.log4j.Logger;
import org.rpi.songcast.common.SongcastMessage;

import io.netty.buffer.ByteBuf;

public class OHUMessageLeave extends SongcastMessage {
	
	private Logger log = Logger.getLogger(this.getClass());
	private SocketAddress address = null;

	
	public OHUMessageLeave(ByteBuf buf, SocketAddress address) {
		super.setData(buf.retain());
		this.setAddress(address);
		log.debug("Leave Message Received from Address: " + address);
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
	public SocketAddress getAddress() {
		return address;
	}


	/**
	 * @param address the address to set
	 */
	private void setAddress(SocketAddress address) {
		this.address = address;
	}

}

