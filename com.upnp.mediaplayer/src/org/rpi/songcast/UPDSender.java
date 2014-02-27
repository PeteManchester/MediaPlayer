package org.rpi.songcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Vector;

import org.apache.log4j.Logger;

class UPDSender implements Runnable {

	private Logger log = Logger.getLogger(this.getClass());
	private boolean run = true;
	private MulticastSocket mSocket = null;

	private int mcastPort = 0;
	private InetAddress mcastAddr = null;
	private String zoneID = "";
	private Vector mWorkQueue = new Vector();
	private String nic = "";

	UPDSender(int port, InetAddress addr, String zoneID,String nic) {
		this.nic = nic;
		mcastPort = port;
		mcastAddr = addr;
		this.zoneID = zoneID;

		try {
			mSocket = new MulticastSocket(port);
			mSocket.setReuseAddress(true);
			//InetAddress inet = InetAddress.getByName("192.168.1.72");
			NetworkInterface netIf = NetworkInterface.getByName(nic);
			mSocket.setNetworkInterface(netIf);
			mSocket.setSoTimeout(5000);
			NetworkInterface ifs = mSocket.getNetworkInterface();
			log.debug("Receiver NetworkInterface: " + ifs.getDisplayName());
		} catch (IOException ioe) {
			log.error("problems creating the datagram socket.", ioe);
		}
	}

	public synchronized boolean isEmpty() {
		return mWorkQueue.isEmpty();
	}

	public synchronized void put(Object object) {
		log.debug("Put Object in WorkQueue " + object.toString());
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
					byte[] command = (byte[]) get();
					log.info("Pulled Command Subscription ");
					processEvent(command);
					log.info("Number of Commands in Queue: " + mWorkQueue.size());
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			} else {
				sleep(100);
			}
		}
	}

	private void processEvent(byte[] bytes) {
		try {
			if (bytes == null)
				return;
			log.debug("Sending Command to : " + mcastAddr.getHostAddress() + ":" + mcastPort + " bytes: " + bytes);
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, mcastAddr, mcastPort);
			mSocket.send(packet);
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				sb.append(String.format("%02X ", b));
			}
			log.debug("sending multicast message: " + sb.toString());
		} catch (Exception e) {
			log.error("Error Sending Message: ", e);
		}
	}
	
	public void disconnect()
	{
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