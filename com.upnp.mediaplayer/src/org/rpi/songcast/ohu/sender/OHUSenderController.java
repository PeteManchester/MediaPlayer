package org.rpi.songcast.ohu.sender;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.sender.messages.OHUMessageJoin;
import org.rpi.songcast.ohu.sender.messages.OHUMessageLeave;
import org.rpi.songcast.ohu.sender.messages.OHUMessageListen;
import org.rpi.songcast.ohu.sender.mpd.MPDStreamerController;
import org.rpi.songcast.ohu.sender.response.OHUSenderSlaveResponse;

import io.netty.channel.socket.DatagramPacket;

public class OHUSenderController {

	private static OHUSenderController instance = null;
	private Logger log = Logger.getLogger(this.getClass());
	private Map<String, OHUSenderConnection> senders = new ConcurrentHashMap<String, OHUSenderConnection>();
	private Map<String, String> sendersListen = new ConcurrentHashMap<String, String>();
	private Map<String, Integer> sendersNaughtyList = new HashMap<String, Integer>();

	private Thread ohuSenderThread = null;
	private OHUSenderThread ohuSender = null;

	private OHUSenderConnection primaryConnection = null;

	private ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

	public static OHUSenderController getInstance() {
		if (instance == null) {
			instance = new OHUSenderController();
		}
		return instance;
	}

	private OHUSenderController() {
		startSenderThread(null);
		startTimer();
	}

	private void startTimer() {
		ses.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					//log.debug("TimerFired");
					checkListenEvents();
				} catch (Exception e) {
					log.error("Timer Error", e);
				}
			}
		}, 3000, 1200, TimeUnit.MILLISECONDS);
	}

	private void stopTimer() {
		ses.shutdownNow();
	}

	private void checkListenEvents() {
		for (String key : senders.keySet()) {
			if (sendersListen.containsKey(key)) {
				if (sendersNaughtyList.containsKey(key)) {
					sendersNaughtyList.remove(key);
				}
			} else {
				log.debug("Sender: " + key + " has NOT sent a Listen Event");
				if (sendersNaughtyList.containsKey(key)) {
					int count = sendersNaughtyList.get(key);
					count++;
					sendersNaughtyList.put(key, count);
					if (sendersNaughtyList.get(key) > 3) {
						log.debug("Remove Receiver: " + key + " No Listen Events have been received on 3 consecutive occasions");
						OHUSenderConnection con = senders.get(key);
						senders.remove(key);
						sendersNaughtyList.remove(key);
						removeClient(key);
						con.stop();
					}
				} else {
					sendersNaughtyList.put(key, 1);
				}
			}
		}
		sendersListen.clear();
	}

	/***
	 * 
	 * @param ohuLeave
	 */
	public void removeClient(OHUMessageLeave ohuLeave) {
		log.debug("Songcast Sender, client Leaving: " + ohuLeave.toString());
		removeClient(ohuLeave.getHostString());
	}

	public void removeClient(String remoteAddress) {
		log.debug("Songcast Sender, being removed: " + remoteAddress);
		if (senders.containsKey(remoteAddress)) {
			senders.get(remoteAddress).setEnabled(false);
		}

		if (primaryConnection != null && remoteAddress.equals(primaryConnection.getRemoteHostString())) {
			log.debug("Songcast Sender, client Left: " + remoteAddress + " Find a New Primary Connection");
			senderChanged(null);
			if (senders.size() == 0) {
				log.debug("Songcast Sender, client Left: " + remoteAddress + "  Removed Songcast Receiver, no other Songcast Receivers to stop sending");

			} else {
				for (String key : senders.keySet()) {
					OHUSenderConnection con = senders.get(key);
					log.debug("Songcast Sender, looking for next PrimaryConnection: " + con);
					if (con.getRemoteAddress() != null && con.isEnabled()) {
						log.debug("Songcast Sender, client Left: " + remoteAddress + " Promoting this Receiver to be Primary: " + key);
						senderChanged(senders.get(key));
					}
				}
			}
		}
		buildSlaveRequest();
	}

	/***
	 * 
	 * @param mJoin
	 */
	public void AddClient(OHUMessageJoin mJoin) {
		log.debug("Songcast Sender, client Join Request: " + mJoin.toString());
		if (senders.containsKey(mJoin.getHostString())) {
			OHUSenderConnection con = senders.get(mJoin.getHostString());
			con.setEnabled(true);
			con.setRemoteAddress(mJoin.getAddress());
			senders.put(mJoin.getHostString(), con);
			if (primaryConnection == null) {
				log.debug("Songcast Sender, client Join Request: " + mJoin.toString() + " No PrimaryConnection, make this PrimaryConnection");
				senderChanged(con);
			} else {

			}
		}
		buildSlaveRequest();
	}

	/***
	 * 
	 * @param listen
	 */
	public void addListen(OHUMessageListen listen) {
		sendersListen.put(listen.getHostString(), listen.getHostString());
	}

	/***
	 * 
	 * @param localInetAddr
	 * @param remoteHost
	 * @return
	 */
	public String startSenderConnection(InetAddress localInetAddr, String remoteHost) {
		log.debug("StartConnection: " + remoteHost);
		if (senders.containsKey(remoteHost)) {
			if (primaryConnection != null && primaryConnection.getRemoteHostString() == remoteHost) {
			}
			log.debug("Start Connection, there is already a Connection for this remoteHost: " + remoteHost);
			return senders.get(remoteHost).getURI();
		}
		log.debug("Create OHUSenderConnection: " + remoteHost);
		OHUSenderConnection con = new OHUSenderConnection(localInetAddr);
		log.debug("Created OHUSenderConnection: " + remoteHost);
		String uri = "";
		try {
			log.debug("Start new OHUSenderConnection: " + remoteHost);
			uri = con.run();
			log.debug("Started new OHUSenderConnection: URI: " + uri);
			senders.put(remoteHost, con);
		} catch (Exception e) {
			log.error("Error Starting OHUSenderConnector:" + remoteHost, e);
		}
		return uri;
	}

	/***
	 * 
	 */
	private void buildSlaveRequest() {
		if (primaryConnection == null) {
			log.debug("Do Not Send SlaveRequest, no RemoteSocket");
			if (senders.size() == 0) {
				log.debug("No Senders, stop MPDConnection");
				try {
					MPDStreamerController.getInstance().stop();
				} catch (Exception e) {
					log.error("Error Stopping MPD", e);
				}
			}
			return;
		}

		log.debug("Build SlaveRequest");
		List<InetSocketAddress> mSlaves = new LinkedList<InetSocketAddress>();
		for (String key : senders.keySet()) {
			OHUSenderConnection ohu = senders.get(key);
			if (ohu.getRemoteAddress() != null) {
				if (!key.equalsIgnoreCase(primaryConnection.getRemoteHostString())) {
					if (ohu.isEnabled()) {
						log.debug("Build SlaveRequest. Adding Slave: " + ohu.getRemoteHostString());
						mSlaves.add(ohu.getRemoteAddress());
					}
				}
			}

		}
		OHUSenderSlaveResponse s = new OHUSenderSlaveResponse(mSlaves);
		DatagramPacket packet = new DatagramPacket(s.getBuffer(), primaryConnection.getRemoteAddress());
		try {
			log.debug("Send SlaveRequest: " + s.toString());
			primaryConnection.sendMessage(packet);
		} catch (Exception e) {
			log.error("Unable to send Slave Message", e);
		}
		MPDStreamerController.getInstance().start();
	}

	/***
	 * 
	 * @param remoteHost
	 */
	public void stopSenderConnection(String remoteHost) {
		if (senders.containsKey(remoteHost)) {
			OHUSenderConnection con = senders.get(remoteHost);
			stopSender(con);
			senders.remove(remoteHost);
		}
	}

	public void senderChanged(OHUSenderConnection ohu) {
		log.debug("Primary Sender changed to: " + ohu);
		this.primaryConnection = ohu;
		ohuSender.setOHUSenderConnector(ohu);
	}

	/***
	 * 
	 * @param con
	 */
	private void stopSender(OHUSenderConnection con) {
		con.stop();
		// stopTimer();
	}

	/***
	 * 
	 * @param con
	 */
	private void startSenderThread(OHUSenderConnection con) {
		ohuSender = new OHUSenderThread(con);
		ohuSenderThread = new Thread(ohuSender, "OHUSenderThread");
		ohuSenderThread.start();
	}

	/***
	 * 
	 */
	private void stopSenderThread() {
		log.debug("Attempt to Stop Songcast Sender");
		try {

			if (ohuSender != null) {
				ohuSender.stop();
				ohuSender = null;
				ohuSenderThread = null;
			}
		} catch (Exception e) {
			log.error("Error Closing OHUSenderThread", e);
		}
	}

}
