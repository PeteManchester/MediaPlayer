package org.rpi.songcast;

import java.net.InetAddress;

import org.apache.log4j.Logger;

public class OHZManager {

	private Logger log = Logger.getLogger(this.getClass());

	private int mcastPort = 51970;

	private InetAddress mcastAddr = null;

	private String zoneID = "";
	
	private Thread tReceiver =null;
	
	private Thread tSender = null;
	
	private UDPReceiver udpReceiver = null;
	private UPDSender udpSender = null;

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
		udpReceiver = new UDPReceiver(mcastPort, mcastAddr, zoneID);
		tReceiver = new Thread(udpReceiver, "McastReceiver");
		//new Thread(new UDPReceiver(mcastPort, mcastAddr, zoneID), "McastReceiver").start();

		tReceiver.start();
		
		while(!udpReceiver.isConnected())
		{
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}

		// start new thread to send multicasts
		udpSender = new UPDSender(mcastPort, mcastAddr, zoneID);
		tSender = new Thread(udpSender, "McastRepeater");
		tSender.start();
		OHZJoin join = new OHZJoin(zoneID);
		udpSender.put(join.data);
	}
	
	
	public void stop()
	{
		udpSender.disconnect();
		udpReceiver.disconnect();
		tReceiver = null;
		tSender = null;
	}

}
