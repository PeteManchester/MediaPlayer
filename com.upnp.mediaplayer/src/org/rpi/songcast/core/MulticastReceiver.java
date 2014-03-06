package org.rpi.songcast.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import org.apache.log4j.Logger;
import org.rpi.songcast.ohm.OHMessage;

public class MulticastReceiver implements Runnable {

	private Logger log = Logger.getLogger(this.getClass());

	int mcastPort = 0;

	InetAddress mcastAddr = null;

	InetAddress localHost = null;

	String zoneID = "";

	private boolean bConnected = false;

	private MulticastSocket mSocket = null;

	private SongcastManager scManager = null;

	private boolean bRunning = true;
	private String nic = "";

	public MulticastReceiver(int port, InetAddress addr, String zoneID, String nic, SongcastManager scManager) {
		this.nic = nic;
		mcastPort = port;
		mcastAddr = addr;
		this.zoneID = zoneID;
		this.scManager = scManager;
	}

	// public MulticastReceiver(MulticastSocket mSocket, InetAddress addr,
	// String zoneID, String nic, SongcastManager scManager) {
	// this.nic = nic;
	// //mcastPort = port;
	// mcastAddr = addr;
	// this.mSocket = mSocket;
	// this.zoneID = zoneID;
	// this.scManager = scManager;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			log.debug("Setting up Multicast Receiver, Port: " + mcastPort);
			// We need to ensure that the Receiver is bound to the correct
			// Network address..
			InetSocketAddress localAddress = new InetSocketAddress("192.168.1.72", mcastPort);
			if (mcastAddr.isMulticastAddress()) {
				mSocket = new MulticastSocket(mcastPort);
			} else {
				mSocket = new MulticastSocket(mcastPort);
				//mSocket = new MulticastSocket();
				//mSocket.setReuseAddress(true);
				//mSocket.bind(localAddress);
			}

			NetworkInterface netIf = NetworkInterface.getByName(nic);
			mSocket.setNetworkInterface(netIf);
			mSocket.setSoTimeout(5000);
			// mSocket.setReuseAddress(true);
			NetworkInterface ifs = mSocket.getNetworkInterface();
			if (mcastAddr.isMulticastAddress()) {
				log.debug("Joining Multicast Group: " + mcastAddr.getHostAddress() + ":" + mcastPort + " NIC: " + ifs.getDisplayName());
				// log.debug("Joining Mutlicast Group: " +
				// mcastAddr.getHostAddress() + ":" + mcastPort );
				mSocket.joinGroup(mcastAddr);
			} else {
				if (!mSocket.isConnected()) {
					log.debug(mSocket.getLocalPort() + mSocket.getLocalAddress().toString());
					mSocket.connect(mcastAddr, mcastPort);
				}
				mSocket.setBroadcast(true);
				log.debug("Not a Multicast Address, do not Join Group");
			}
			setConnected(true);
		} catch (Exception ioe) {
			log.error("Trouble opening multicast port", ioe);
		}

		DatagramPacket packet;
		log.debug("UDPReciever receiver set up ");
		while (bRunning) {
			try {
				// byte[] buf = new byte[16392];
				byte[] buf = new byte[1828];
				packet = new DatagramPacket(buf, buf.length);
				// log.debug("Ready To Receive");
				mSocket.receive(packet);
				byte[] data = packet.getData();
				OHMessage mess = new OHMessage();
				mess.data = data;
				mess.checkMessageType(scManager);
			} catch (Exception e) {
				log.error("Trouble reading multicast message " + mSocket.getLocalPort() + " " + e.getMessage());
			}
		}

	}

	/*
	 * 
	 */
	public boolean isConnected() {
		return bConnected;
	}

	/*
	 * 
	 */
	private void setConnected(boolean bConnected) {
		this.bConnected = bConnected;
	}

	/*
	 * 
	 */
	public void disconnect() {
		bRunning = false;
		if (mSocket != null) {
			try {
				mSocket.leaveGroup(mcastAddr);
			} catch (IOException e) {
				log.error("Error Leave Group");
			}
			try {
				mSocket.close();
			} catch (Exception e) {
				log.error("Error Close");
			}
		}
	}

	// @Override
	// /*
	// * (non-Javadoc)
	// *
	// * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	// */
	// public void update(Observable paramObservable, Object ev) {
	// EventSongCastBase e = (EventSongCastBase) ev;
	// fireEvent(e);
	// }

	/**
	 * Fire the Events
	 * 
	 * @param ev
	 */
	// public void fireEvent(EventSongCastBase ev) {
	// setChanged();
	// notifyObservers(ev);
	// }

}