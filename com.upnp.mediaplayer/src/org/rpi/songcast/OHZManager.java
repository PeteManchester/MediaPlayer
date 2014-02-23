package org.rpi.songcast;

import java.net.InetAddress;

import org.apache.log4j.Logger;

public class OHZManager {

	private Logger log = Logger.getLogger(this.getClass());

	private int mcastPort = 51970;

	private InetAddress mcastAddr = null;

	private String zoneID = "";
	
	private Thread receiver =null;
	
	private Thread sender = null;

	public OHZManager(String uri, String zoneID) {
		try {
			int lastColon = uri.lastIndexOf(":");
			int lastSlash = uri.lastIndexOf("/");
			String host = uri.substring(lastSlash + 1, lastColon);
			String sPort = uri.substring(lastColon + 1);
			mcastAddr = InetAddress.getByName(host);
			mcastPort = Integer.parseInt(sPort);
			this.zoneID = zoneID;
		} catch (Exception e) {
			log.error(e);
		}

	}

	public void start() {
		// start new thread to receive multicasts

		receiver = new Thread(new UDPReceiver(mcastPort, mcastAddr, zoneID), "McastReceiver");;
		//new Thread(new UDPReceiver(mcastPort, mcastAddr, zoneID), "McastReceiver").start();
		receiver.start();

		// start new thread to send multicasts
		UPDSender mSender = new UPDSender(mcastPort, mcastAddr, zoneID);
		sender = new Thread(mSender, "McastRepeater");
		sender.start();
		OHZJoin join = new OHZJoin(zoneID);
		mSender.put(join.data);
	}
	
	
	public void stop()
	{
		receiver = null;
		sender = null;
	}

}
