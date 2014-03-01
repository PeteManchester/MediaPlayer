package org.rpi.songcast;

import java.net.InetAddress;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

public class OHMManager implements Observer {
	
	private Logger log = Logger.getLogger(this.getClass());

	private int mcastPort = 51970;

	private InetAddress mcastAddr = null;

	private String zoneID = "";
	
	private Thread tReceiver =null;
	
	private Thread tSender = null;
	
	private UDPReceiver udpReceiver = null;
	private UDPSender udpSender = null;
	private String nic = "";

	public OHMManager(String uri, String zoneID,String nic) {
		try {
			this.nic = nic;
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
		udpReceiver = new UDPReceiver(mcastPort, mcastAddr, zoneID,nic);
		tReceiver = new Thread(udpReceiver, "OHMMcastReceiver");
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
		udpSender = new UDPSender(mcastPort, mcastAddr, zoneID,nic);
		tSender = new Thread(udpSender, "OHMMcastRepeater");
		tSender.start();
		//OHZJoin join = new OHZJoin(zoneID);
		OHMJoin join = new OHMJoin(zoneID);
		join.addObserver(this);
		udpSender.put(join.data);
	}
	
	
	public void stop()
	{
		udpSender.disconnect();
		udpReceiver.disconnect();
		tReceiver = null;
		tSender = null;
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}
}
