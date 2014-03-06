package org.rpi.songcast.ohm;

import java.net.InetAddress;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;
import org.rpi.songcast.core.MulticastReceiver;
import org.rpi.songcast.core.MutlicastSender;
import org.rpi.songcast.core.SongcastManager;
import org.rpi.songcast.core.SongcastPlayerJavaSound;
import org.rpi.songcast.core.SongcastSocket;
import org.rpi.songcast.events.EventSongCastBase;

public class OHMManager implements Observer, SongcastManager {

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
		// start new thread to receive multicasts
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

		threadMessageQueue = new Thread(mq, "OHMMessageQueue");
		mq.addObserver(this);
		threadMessageQueue.start();
		songcastSocket = new SongcastSocket(mcastPort, mcastAddr, zoneID, nic, this);
		tReceiver = new Thread(songcastSocket, "OHMMcastReceiver");
		//udpReceiver.addObserver(this);
		tReceiver.start();

//		while (!udpReceiver.isConnected()) {
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//			}
//		}

		// start new thread to send multicasts
//		udpSender = new MutlicastSender(mcastPort, mcastAddr, zoneID, nic);
//		//udpSender = new MutlicastSender(mSocket,mcastAddr, zoneID, nic);
//		tSender = new Thread(udpSender, "OHMMcastRepeater");
//		tSender.start();
//		SongcastPlayerJavaSound.getInstance().createSoundLine();
//		//OHMRequestListen listen = new OHMRequestListen(zoneID);
		OHMRequestJoin join = new OHMRequestJoin(zoneID);
		//join.addObserver(this);
		songcastSocket.put(join);
		// TODO maybe move this Playing status....
		PlayManager.getInstance().setStatus("Playing");
		startTimer();
	}

	public void disconnect() {
		SongcastPlayerJavaSound.getInstance().stop();
		if (songcastSocket != null) {
			songcastSocket.disconnect();
		}
//		if (udpReceiver != null) {
//			udpReceiver.disconnect();
//		}
		tReceiver = null;
		tSender = null;
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
	}

	@Override
	public void update(Observable o, Object ev) {
		EventSongCastBase e = (EventSongCastBase) ev;
		switch (e.getType()) {
		case EVENT_OHM_AUDIO_STARTED:
			if (ohuRequester == null) {
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
