package org.rpi.songcast.ohu.sender.messages;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.rpi.songcast.common.SongcastMessage;

import io.netty.buffer.ByteBuf;

/***
 * Join Message sent by the Songcast Receiver, they want us to start sending to
 * them.
 * 
 * @author phoyle
 *
 */

public class OHUMessageJoin extends SongcastMessage {

	private Logger log = Logger.getLogger(this.getClass());
	private InetSocketAddress address = null;
	private String hostString = "";

	public OHUMessageJoin(ByteBuf buf, InetSocketAddress address) {
		//super.setData(buf.retain());
		this.setAddress(address);
		log.debug("Join Message Received from Address: " + address);
		//buf.release();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OHUMessageJoin [hostString=");
		builder.append(hostString);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the address
	 */
	public InetSocketAddress getAddress() {
		return address;
	}
	
	public String getURI() {
		return hostString + ":" + address.getPort();
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
	 * @param address
	 *            the address to set
	 */
	private void setAddress(InetSocketAddress address) {
		this.address = address;
		this.hostString = address.getHostString();
	}
}
