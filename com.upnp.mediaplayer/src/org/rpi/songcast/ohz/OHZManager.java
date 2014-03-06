package org.rpi.songcast.ohz;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;
import org.rpi.songcast.core.SongcastManager;
import org.rpi.songcast.core.MulticastReceiver;
import org.rpi.songcast.core.MutlicastSender;
import org.rpi.songcast.core.SongcastSocket;
import org.rpi.songcast.events.EventOHZIURI;
import org.rpi.songcast.events.EventSongCastBase;
import org.rpi.songcast.ohm.OHMManager;

public class OHZManager implements  SongcastManager {

	private Logger log = Logger.getLogger(this.getClass());

	private int mcastPort = 51970;

	private InetAddress mcastAddr = null;

	private String zoneID = "";

	private Thread tReceiver = null;

	private Thread tSender = null;

	//private MulticastReceiver udpReceiver = null;
	//private MutlicastSender udpSender = null;
	private SongcastSocket songcastSocket = null;
	private String nic = "";
	private OHZRequestJoin join = null;
	private OHMManager ohm = null;


	public OHZManager(String uri, String zoneID, String nic) {
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
		
//		MulticastSocket mSocket = null;
//		try {
//			mSocket = new MulticastSocket(mcastPort);
//			NetworkInterface netIf = NetworkInterface.getByName(nic);
//			mSocket.setReuseAddress(true);
//			//mSocket.setSoTimeout(5000);
//			mSocket.setNetworkInterface(netIf);
//			mSocket.setReuseAddress(true);
//		} catch (Exception e) {
//			log.error("Error Creating MulticastSocket", e);
//		}

		// start new thread to receive multicasts
		//udpReceiver = new MulticastReceiver(mSocket,mcastAddr, zoneID, nic, this);
		songcastSocket = new SongcastSocket(mcastPort, mcastAddr, zoneID, nic, this);
		tReceiver = new Thread(songcastSocket, "McastReceiver");
		//udpReceiver.addObserver(this);
		tReceiver.start();

//		while (!udpReceiver.isConnected()) {
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//			}
//		}

		// start new thread to send multicasts
		//udpSender = new MutlicastSender(mSocket,mcastAddr, zoneID, nic);
//		udpSender = new MutlicastSender(mcastPort, mcastAddr, zoneID, nic);
//		tSender = new Thread(udpSender, "McastRepeater");
//		tSender.start();
		OHZRequestJoin join = new OHZRequestJoin(zoneID);
		songcastSocket.put(join);
	}

	public void disconnect() {
		stop(zoneID);
		if (songcastSocket != null) {
			songcastSocket.disconnect();
		}
//		if (udpReceiver != null) {
//			udpReceiver.disconnect();
//		}
		
		tReceiver = null;
		tSender = null;
		if(ohm !=null)
		{
			ohm.disconnect();
			ohm = null;
		}
		
	}

//	@Override
//	public void update(Observable arg0, Object ev) {
//		EventSongCastBase e = (EventSongCastBase) ev;
//		switch (e.getType()) {
//		case EVENT_OHZ_URI:
//			EventOHZIURI ohz = (EventOHZIURI) e;
//			connectToOHM(ohz.getUri(), ohz.getZone());
//			break;
//		}
//
//	}

	public void connectToOHM(String uri, String zone) {
		if (ohm != null) {
			//if (ohm.getZoneID().equalsIgnoreCase(zone)) {
				log.debug("Already Connected to Zone: " + zone);
				return;
			//}
		}
		log.debug("Not Connected to Zone, attempt to connect: " + zone);
		PlayManager.getInstance().setStatus("Buffering");
		ohm = new OHMManager(uri, zone, nic);
		ohm.start();
	}

	public void stop(String zoneID) {
		if (ohm == null)
			return;
		ohm.disconnect();
		ohm = null;
		disconnect();
	}

	public void putMessage(byte[] data) {
		int iType = new BigInteger(getBytes(5, 5, data)).intValue();
		switch (iType) {
		case 0:
			log.warn("OHZ URL Requst arrived at Receiver");
			break;
		case 1:// ZoneURI
			log.warn("OHZ Zone URI");
			OHZEventZoneURI resURI = new OHZEventZoneURI();
			resURI.data = data;
			resURI.checkMessageType();
			if (resURI.isOKToConnect()) {
				connectToOHM(resURI.getUri(), resURI.getZoneName());
			}
			break;

		default:
			log.debug("OHZ Message: " + iType);
			break;
		}
	}

	/*
	 * DUPLICATE refactor later.. Get a portion of the bytes in the array.
	 */
	public byte[] getBytes(int start, int end, byte[] data) {
		int size = (end - start) + 1;
		int count = 0;
		byte[] res = new byte[size];
		for (int i = start; i <= end; i++) {
			res[count] = data[i];
			count++;
		}
		return res;
	}

}
