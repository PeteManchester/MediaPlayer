package org.rpi.songcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

class UDPReceiver implements Runnable {

	private Logger log = Logger.getLogger(this.getClass());

	int mcastPort = 0;

	InetAddress mcastAddr = null;

	InetAddress localHost = null;

	String zoneID = "";
	
	private boolean bConnected = false;
	
	private MulticastSocket mSocket = null;
	
	private boolean bRunning = true;

	public UDPReceiver(int port, InetAddress addr, String zoneID) {

		mcastPort = port;

		mcastAddr = addr;
		this.zoneID = zoneID;

		try {
			// TODO set own IP Address
			localHost = InetAddress.getByName("192.168.1.72");

		} catch (UnknownHostException uhe) {

			log.error("Problems identifying local host", uhe);

		}

	}

	public void run() {

		

		try {
			log.debug("Setting up multicast receiver");
			//We need to ensure that the Receiver is bound to the correct Network address..
			mSocket = new MulticastSocket(mcastPort);
			InetAddress inet = InetAddress.getByName("192.168.1.72");
			NetworkInterface netIf = NetworkInterface.getByInetAddress(inet);
			mSocket.setNetworkInterface(netIf);
			mSocket.setReuseAddress(true);
			NetworkInterface ifs = mSocket.getNetworkInterface();
			log.debug("Receiver NetworkInterface: " + ifs.getDisplayName());
			log.debug("Joining Mutlicast Group: " + mcastAddr.getHostAddress() + ":" + mcastPort);
			mSocket.joinGroup(mcastAddr);
			setConnected(true);
		} catch (IOException ioe) {
			log.error("Trouble opening multicast port", ioe);
		}

		DatagramPacket packet;
		log.debug("Multicast receiver set up ");
		while (bRunning) {
			try {
				byte[] buf = new byte[1000];
				packet = new DatagramPacket(buf, buf.length);
				log.debug("McastReceiver: waiting for packet");
				mSocket.receive(packet);
				log.debug("Received Packet: " + packet.getAddress().getHostAddress());
				byte[] data = packet.getData();
				StringBuilder sb = new StringBuilder();
				for (byte b : data) {
					sb.append(String.format("%02X ", b));
				}
				log.debug("Received: " + sb.toString());
				String text = new String(data, "UTF-8");
				log.debug("Received: " + text);
				OHMessage mess = new OHMessage();
				mess.data = data;
				mess.checkMessageType();
				// ignore packets from myself, print the rest
				log.debug("Address: " + packet.getAddress().getHostAddress());
				if (!(packet.getAddress().equals(localHost))) {
					log.debug("Recevied from: " + packet.getAddress());
				} else {
					log.debug("That was a Localhost:");
				}
			} catch (Exception e) {
				log.error("Trouble reading multicast message", e);
			}
		}

	}

	public boolean isConnected() {
		return bConnected;
	}

	private void setConnected(boolean bConnected) {
		this.bConnected = bConnected;
	}
	
	public void disconnect()
	{
		bRunning = false;
		if(mSocket !=null)
		{
			try {
				mSocket.leaveGroup(mcastAddr);
			} catch (IOException e) {
				log.error("Error Leave Group");
			}
			try
			{
			mSocket.close();
			}
			catch(Exception e)
			{
				log.error("Error Close");
			}
		}
	}

}