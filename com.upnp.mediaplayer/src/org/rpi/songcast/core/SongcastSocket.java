package org.rpi.songcast.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohm.OHMRequestLeave;
import org.rpi.songcast.ohm.OHMessage;

public class SongcastSocket implements Runnable {

	private Logger log = Logger.getLogger(this.getClass());
	private boolean run = true;
	private MulticastSocket mSocket = null;

	private int mcastPort = 0;
	private InetAddress mcastAddr = null;
	private String zoneID = "";
	private Vector mWorkQueue = new Vector();
	private String nic = null;
	private SongcastManager scManager = null;
	private Receive receiver = null;

	public SongcastSocket(int port, InetAddress addr, String zoneID, String nic, SongcastManager scManager) {
		this.nic = nic;
		mcastPort = port;
		mcastAddr = addr;
		this.zoneID = zoneID;
		this.scManager = scManager;

		try {
			// InetSocketAddress localAddress = new
			// InetSocketAddress("192.168.1.72", mcastPort);
			if (addr.isMulticastAddress()) {
				mSocket = new MulticastSocket(mcastPort);
			} else {
				mSocket = new MulticastSocket();
			}
			// mSocket.setReuseAddress(true);
			NetworkInterface netIf = NetworkInterface.getByName(nic);
			mSocket.setNetworkInterface(netIf);
			// log.debug("ReUseAddress: " + mSocket.getReuseAddress());
			// mSocket.setSoTimeout(5000);
			NetworkInterface ifs = mSocket.getNetworkInterface();
			log.debug("Receiver NetworkInterface: " + ifs.getDisplayName());
			if (addr.isMulticastAddress()) {
				mSocket.joinGroup(mcastAddr);
			} else {
			}
			receiver = new Receive(scManager);
		} catch (IOException ioe) {
			log.error("problems creating the datagram socket. " + mSocket.getLocalAddress().toString(), ioe);

		}
	}

	public synchronized boolean isEmpty() {
		return mWorkQueue.isEmpty();
	}

	public synchronized void put(Object object) {
		// log.debug("Put Object in WorkQueue " + object.toString());
		try {
			mWorkQueue.addElement(object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Get the first object out of the queue. Return null if the queue is empty.
	 */
	public synchronized Object get() {
		Object object = peek();
		if (object != null)
			mWorkQueue.removeElementAt(0);
		return object;
	}

	/**
	 * Peek to see if something is available.
	 */
	public Object peek() {
		if (isEmpty())
			return null;
		return mWorkQueue.elementAt(0);
	}

	private void sleep(int value) {
		try {
			Thread.sleep(value);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	public synchronized void clear() {
		try {
			log.info("Clearing Work Queue. Number of Items: " + mWorkQueue.size());
			mWorkQueue.clear();
			log.info("WorkQueue Cleared");
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
		}
	}

	public void run() {
			while (run) {
				if (!isEmpty()) {
					try {
						SongcastMessage mess = (SongcastMessage) get();
						processEvent(mess);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				} else {
					sleep(100);
				}
			}
	}

	private void processEvent(SongcastMessage mess) {
		try {

			byte[] bytes = mess.data;
			if (bytes == null)
				return;
			if (mess instanceof OHMRequestLeave) {
				log.debug("OHM Leave Message being Sent");
			}
			// log.debug("Sending Command to : " + mcastAddr.getHostAddress() +
			// ":" + mcastPort + " " + mess.toString());
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, mcastAddr, mcastPort);
			mSocket.send(packet);
		} catch (Exception e) {
			log.error("Error Sending Message: ", e);
		}
	}

	public void dispose() {
		run = false;
		if (receiver != null) {
			receiver.stopThread();
			receiver = null;
		}
		if (mSocket != null) {
			try {
				if (mcastAddr.isMulticastAddress()) {
					mSocket.leaveGroup(mcastAddr);
				}
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

	class Receive extends Thread {
		private SongcastManager scManager = null;
		private boolean run = true;

		Receive(SongcastManager scManager) {
			log.debug("Start of SongcastReceive Thread");
			this.scManager = scManager;
			start();
		}

		public void run() {
			while (run) {
				try {
					byte b[] = new byte[1828];
					DatagramPacket packet = new DatagramPacket(b, b.length);
					mSocket.receive(packet);
					byte[] data = packet.getData();
					OHMessage mess = new OHMessage();
					mess.data = data;
					mess.checkMessageType(scManager);
					// log.debug("From " + packet.getPort() + ": " + new
					// String(packet.getData(), 0, packet.getLength()));
				} catch (Exception e) {

				}
			}

		}

		public void stopThread() {
			run = false;
		}
	}

}