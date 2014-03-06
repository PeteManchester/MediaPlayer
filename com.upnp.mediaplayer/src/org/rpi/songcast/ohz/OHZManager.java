package org.rpi.songcast.ohz;

import java.math.BigInteger;
import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;
import org.rpi.songcast.core.SongcastManager;
import org.rpi.songcast.core.SongcastSocket;
import org.rpi.songcast.ohm.OHMManager;

public class OHZManager implements  SongcastManager {

	private Logger log = Logger.getLogger(this.getClass());

	private int mcastPort = 51970;

	private InetAddress mcastAddr = null;

	private String zoneID = "";

	private Thread threadSocket = null;

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
		songcastSocket = new SongcastSocket(mcastPort, mcastAddr, zoneID, nic, this);
		threadSocket = new Thread(songcastSocket, "OHZSongcastSocket");
		threadSocket.start();
		OHZRequestJoin join = new OHZRequestJoin(zoneID);
		songcastSocket.put(join);
	}

	public void dispose() {
		stop(zoneID);
		if (songcastSocket != null) {
			songcastSocket.dispose();
		}
		
		if(ohm !=null)
		{
			ohm.dispose();
			ohm = null;
		}
		threadSocket.interrupt();
		threadSocket = null;
	}

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
		ohm.dispose();
		ohm = null;
		dispose();
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
