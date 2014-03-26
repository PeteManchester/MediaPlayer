package org.rpi.songcast.ohm;

import java.net.InetAddress;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;
import org.rpi.songcast.core.SongcastManager;
import org.rpi.songcast.core.SongcastPlayerJSLatency;
import org.rpi.songcast.core.SongcastSocket;
import org.rpi.songcast.events.EventSongCastBase;

public class OHMManager implements Observer, SongcastManager {

	private Logger log = Logger.getLogger(this.getClass());

	private int mcastPort = 51970;

	private InetAddress mcastAddr = null;

	private String zoneID = "";

	private Thread threadSocket = null;
	private SongcastSocket songcastSocket = null;

	private String nic = "";

	private SongcastTimer timer = null;
	private Thread timerThread = null;

	private OHUThreadRequester ohuRequester = null;
	private Thread ohuThread = null;

	private OHMMessageQueue mq = new OHMMessageQueue();
	Thread threadMessageQueue = null;
	


	public OHMManager(String uri, String zoneID, String nic) {
		try {
			log.warn("Creating OHM Manager");
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
		
		threadMessageQueue = new Thread(mq, "OHMMessageQueue");
		mq.addObserver(this);
		threadMessageQueue.start();
		songcastSocket = new SongcastSocket(mcastPort, mcastAddr, zoneID, nic, this);
		threadSocket = new Thread(songcastSocket, "OHMSongcastSocket");

		threadSocket.start();

		OHMRequestJoin join = new OHMRequestJoin(zoneID);
		songcastSocket.put(join);
		OHMRequestListen listen = new OHMRequestListen(zoneID);
		songcastSocket.put(listen);
		startTimer();
		PlayManager.getInstance().setStatus("Playing","SONGCAST");
	}

	public void dispose() {
		OHMRequestLeave leave = new OHMRequestLeave(zoneID);
		songcastSocket.put(leave);
		

		if (songcastSocket != null) {
			songcastSocket.dispose();
		}		

		if (mq != null) {
			mq.clear();
			mq.stop();
		}
		mq = null;
		threadMessageQueue = null;
		if (timer != null) {
			timer.setRun(false);
			timer = null;
		}
		if (timerThread != null) {
			timerThread = null;
		}

		if (ohuRequester != null) {
			ohuRequester.setRun(false);
			ohuRequester = null;
		}

		if (ohuThread != null) {
			ohuThread = null;
		}
		threadSocket.interrupt();
		threadSocket = null;
	}

	@Override
	public void update(Observable o, Object ev) {
		EventSongCastBase e = (EventSongCastBase) ev;
		switch (e.getType()) {
		case EVENT_OHM_AUDIO_STARTED:
			if (ohuRequester == null) {
				log.debug("Audio Started");
				ohuRequester = new OHUThreadRequester(songcastSocket);
				ohuThread = new Thread(ohuRequester, "ohuRequester");
				ohuThread.start();
			}
			break;
		}
	}

	/**
	 * Not used at the moment.
	 * 
	 * @param zoneID
	 */
	public void stop(String zoneID) {
		log.debug("Stop");
	}

	public void putMessage(byte[] data) {
		mq.put(data);
	}

	private void startTimer() {
		timer = new SongcastTimer();
		timerThread = new Thread(timer, "SongcastTimer");
		timerThread.start();
	}

	public String getZoneID() {
		return zoneID;
	}
}
