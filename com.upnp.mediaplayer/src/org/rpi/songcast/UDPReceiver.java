package org.rpi.songcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.songcast.events.EventSongCastBase;

class UDPReceiver extends Observable implements Runnable, Observer  {

	private Logger log = Logger.getLogger(this.getClass());

	int mcastPort = 0;

	InetAddress mcastAddr = null;

	InetAddress localHost = null;

	String zoneID = "";
	
	private boolean bConnected = false;
	
	private MulticastSocket mSocket = null;
	
	private OHMManager ohmManager = null;
	
	private boolean bRunning = true;
	private String nic = "";

	/*
	 * 
	 */
	public UDPReceiver(int port, InetAddress addr, String zoneID,String nic) {
		this.nic = nic;
		mcastPort = port;
		mcastAddr = addr;
		this.zoneID = zoneID;
	}
	
	public UDPReceiver(int port, InetAddress addr, String zoneID,String nic, OHMManager ohmManager) {
		this.nic = nic;
		mcastPort = port;
		mcastAddr = addr;
		this.zoneID = zoneID;
		this.ohmManager = ohmManager;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			log.debug("Setting up multicast receiver");
			//We need to ensure that the Receiver is bound to the correct Network address..
			mSocket = new MulticastSocket(mcastPort);
			NetworkInterface netIf = NetworkInterface.getByName(nic);
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
		log.debug("UDPReciever receiver set up ");
		while (bRunning) {
			try {
				byte[] buf = new byte[16392];
				packet = new DatagramPacket(buf, buf.length);
				mSocket.receive(packet);
				byte[] data = packet.getData();
				OHMessage mess = new OHMessage();
				mess.addObserver(this);
				mess.data = data;
				mess.checkMessageType(ohmManager);
			} catch (Exception e) {
				log.error("Trouble reading multicast message", e);
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

	@Override
	/*
	 * (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable paramObservable, Object ev) {
		EventSongCastBase e = (EventSongCastBase)ev;
		fireEvent(e);
	}
	
	/**
	 * Fire the Events
	 * @param ev
	 */
	public void fireEvent(EventSongCastBase ev) {
		setChanged();
		notifyObservers(ev);
	}

}