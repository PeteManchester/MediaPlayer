package org.rpi.songcast.ohu.sender.messages;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.log4j.Logger;
import org.rpi.songcast.common.SongcastMessage;

import io.netty.buffer.ByteBuf;

public class OHUMessageLeave extends SongcastMessage {
	
	private Logger log = Logger.getLogger(this.getClass());
	private InetSocketAddress address = null;
	private String hostString = "";

	
	public OHUMessageLeave(ByteBuf buf, InetSocketAddress address) {
		//super.setData(buf.retain());
		this.setAddress(address);
		log.debug("Leave Message Received from Address: " + address);
		buf.release();
	}

	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OHUMessageLeave [hostString=");
		builder.append(hostString);
		builder.append("]");
		return builder.toString();
	}


	/**
	 * @return the address
	 */
	public SocketAddress getAddress() {
		return address;
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
	 * @param address the address to set
	 */
	private void setAddress(InetSocketAddress address) {
		this.address = address;
		this.hostString = address.getHostString();
	}

}

