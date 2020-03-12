package org.rpi.songcast.ohu.sender.messages;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.log4j.Logger;
import org.rpi.songcast.common.SongcastMessage;

import io.netty.buffer.ByteBuf;

public class OHUMessageLeave extends SongcastMessage {
	
	private Logger log = Logger.getLogger(this.getClass());
	private InetSocketAddress address = null;

	
	public OHUMessageLeave(ByteBuf buf, InetSocketAddress address) {
		super.setData(buf.retain());
		this.setAddress(address);
		log.debug("Leave Message Received from Address: " + address);
	}

	
	@Override
	public String toString()
	{
		return "OHUMessageLeave";
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
	private void setAddress(InetSocketAddress address) {
		this.address = address;
	}

}

