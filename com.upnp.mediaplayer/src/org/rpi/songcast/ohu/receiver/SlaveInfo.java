package org.rpi.songcast.ohu.receiver;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class SlaveInfo {
	
	private InetAddress address = null;
	private int port = -99;
	private InetSocketAddress remoteAddress = null;
	
	public SlaveInfo(InetAddress address, int port) {
		this.address = address;
		this.port = port;
		remoteAddress = new InetSocketAddress(address, port);
	}
	
	public String getName() {

		return address.getHostAddress() + ":" + port;
	}

	/**
	 * @return the remoteAddress
	 */
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	
	@Override
	public String toString()
	{
		return address.getHostAddress() + ":" + port;
	}



}
