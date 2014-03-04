package org.rpi.songcast.ohm;

import java.net.InetAddress;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;
import org.rpi.songcast.core.SongcastManager;
import org.rpi.songcast.core.SongcastPlayerJavaSound;
import org.rpi.songcast.core.UDPReceiver;
import org.rpi.songcast.core.UDPSender;

public class OHMManager implements Observer, SongcastManager {

	private Logger log = Logger.getLogger(this.getClass());

	private int mcastPort = 51970;

	private InetAddress mcastAddr = null;

	private String zoneID = "";

	private Thread tReceiver = null;

	private Thread tSender = null;

	private UDPReceiver udpReceiver = null;
	private UDPSender udpSender = null;
	private String nic = "";

	private SongcastTimer timer = null;
	private Thread timerThread = null;

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
		threadMessageQueue = new Thread(mq, "OHMMessageQueue");
		threadMessageQueue.start();
		udpReceiver = new UDPReceiver(mcastPort, mcastAddr, zoneID, nic, this);
		tReceiver = new Thread(udpReceiver, "OHMMcastReceiver");
		udpReceiver.addObserver(this);
		tReceiver.start();

		while (!udpReceiver.isConnected()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}

		// start new thread to send multicasts
		udpSender = new UDPSender(mcastPort, mcastAddr, zoneID, nic);
		tSender = new Thread(udpSender, "OHMMcastRepeater");
		tSender.start();
		// OHZJoin join = new OHZJoin(zoneID);
		OHMRequestJoin join = new OHMRequestJoin(zoneID);
		join.addObserver(this);
		SongcastPlayerJavaSound.getInstance().createSoundLine();
		udpSender.put(join.data);
		// TODO maybe move this Playing status....
		PlayManager.getInstance().setStatus("Playing");
		startTimer();
	}

	public void disconnect() {
		SongcastPlayerJavaSound.getInstance().stop();
		if (udpSender != null) {
			udpSender.disconnect();
		}
		if (udpReceiver != null) {
			udpReceiver.disconnect();
		}
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
	}

	@Override
	public void update(Observable o, Object ev) {

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
