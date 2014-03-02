package org.rpi.songcast;

import java.net.InetAddress;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.songcast.events.EventSongCastBase;

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
		udpReceiver.addObserver(this);
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
		OHMRequestJoin join = new OHMRequestJoin(zoneID);
		join.addObserver(this);
		SongcastPlayerJavaSound.getInstance().createFile();
		udpSender.put(join.data);
	}
	
	
	public void disconnect()
	{
		SongcastPlayerJavaSound.getInstance().stop();
		udpSender.disconnect();
		udpReceiver.disconnect();
		tReceiver = null;
		tSender = null;
	}

	@Override
	public void update(Observable o, Object ev) {
	}

	/**
	 * Not used at the moment.
	 * @param zoneID
	 */
	public void stop(String zoneID) {

	}
}
