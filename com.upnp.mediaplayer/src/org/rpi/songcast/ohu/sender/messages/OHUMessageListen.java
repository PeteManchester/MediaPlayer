package org.rpi.songcast.ohu.sender.messages;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.log4j.Logger;
import org.rpi.songcast.common.SongcastMessage;


public class OHUMessageListen extends SongcastMessage {
	
	private Logger log = Logger.getLogger(this.getClass());
	private InetSocketAddress address = null;
	private String hostString = "";

	
	public OHUMessageListen(InetSocketAddress address) {
		this.setAddress(address);
		//log.debug("Listen Message Received from Address: " + address);
	}

	
	@Override
	public String toString()
	{
		return "OHUMessageSlave";
	}
	
	/***
	 * Host String
	 * 
	 * @return
	 */
	public String getHostString() {
		return hostString;
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
		this.hostString = address.getHostString();
	}
	
}
