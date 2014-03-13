package org.rpi.songcast.core;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import org.apache.log4j.Logger;
import org.rpi.config.Config;

public class SlaveEndpoint {

	private Logger log = Logger.getLogger(this.getClass());

	private InetAddress address = null;
	private int port = -99;
	private MulticastSocket mSocket = null;

	public SlaveEndpoint(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}

	public void createSocket() {
		try {
			mSocket = new MulticastSocket();
			NetworkInterface netIf = NetworkInterface.getByName(Config.songcastNICName);
			mSocket.setNetworkInterface(netIf);
		} catch (Exception e) {
			log.error(e);
		}
	}

	/**
	 * @return the address
	 */
	public InetAddress getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(InetAddress address) {
		this.address = address;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	public String getName() {

		return address.getHostAddress() + ":" + port;
	}

	public String toString() {
		return address.getHostAddress() + ":" + port;
	}

	/**
	 * @return the mSocket
	 */
	public MulticastSocket getSocket() {
		return mSocket;
	}

	/**
	 * @param mSocket
	 *            the mSocket to set
	 */
	private void setSocket(MulticastSocket mSocket) {
		this.mSocket = mSocket;
	}

	public void sendData(byte[] data) {
		if(mSocket==null)
			return;
		try {
			DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
			mSocket.send(packet);
		} catch (Exception e) {
			log.error("Sending Packet", e);
		}
	}

	/**
	 * Dispose of the Endpont
	 */
	public void dispose() {
		try
		{
			if(mSocket !=null)
			{
				mSocket.disconnect();
				mSocket = null;
			}
		}
		catch(Exception e)
		{
			log.error("Error disposing of Endpoint: " , e);
		}		
	}

}
