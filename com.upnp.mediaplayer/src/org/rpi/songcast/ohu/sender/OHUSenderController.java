package org.rpi.songcast.ohu.sender;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.receiver.messages.OHUMessageSlave;
import org.rpi.songcast.ohu.sender.messages.OHUMessageJoin;
import org.rpi.songcast.ohu.sender.messages.OHUMessageLeave;
import org.rpi.songcast.ohu.sender.mpd.MPDStreamerConnector;
import org.rpi.songcast.ohu.sender.mpd.MPDStreamerController;
import org.rpi.songcast.ohu.sender.response.OHUSenderSlave;

import io.netty.channel.socket.DatagramPacket;

public class OHUSenderController {

	private static OHUSenderController instance = null;
	private Logger log = Logger.getLogger(this.getClass());
	private ConcurrentHashMap<String, OHUSenderConnection> senders = new ConcurrentHashMap<String, OHUSenderConnection>();

	private Thread ohuSenderThread = null;
	private OHUSenderThread ohuSender = null;

	private OHUSenderConnection primaryConnection = null;
	MPDStreamerConnector mpdClient = null;

	public static OHUSenderController getInstance() {
		if (instance == null) {
			instance = new OHUSenderController();
		}
		return instance;
	}

	private OHUSenderController() {
		startSenderThread(null);
	}

	/***
	 * 
	 * @param ohuLeave
	 */
	public void removeClient(OHUMessageLeave ohuLeave) {
		log.debug("Songcast Sender, client Leaving: " + ohuLeave.toString());
		if (senders.containsKey(ohuLeave.getHostString())) {
			senders.get(ohuLeave.getHostString()).setEnabled(false);
		}


		if (primaryConnection !=null && ohuLeave.getHostString().equals(primaryConnection.getRemoteHostString())) {
			log.debug("Songcast Sender, client Left: " + ohuLeave.toString() + " Find a New Primary Connection");
			senderChanged(null);
			if (senders.size() == 0) {
				log.debug("Songcast Sender, client Left: " + ohuLeave.toString() + "  Removed Songcast Receiver, no other Songcast Receivers to stop sending");

			} else {
				for (String key : senders.keySet()) {
					OHUSenderConnection con = senders.get(key);
					if (con.getRemoteAddress() != null && con.isEnabled()) {
						log.debug("Songcast Sender, client Left: " + ohuLeave.toString() + " Promoting this Receiver to be Primary: " + key);
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
	 * @param localInetAddr
	 * @param remoteHost
	 * @return
	 */
	public String startSenderConnection(InetAddress localInetAddr, String remoteHost) {
		log.debug("StartConnection: " + remoteHost);
		if (senders.containsKey(remoteHost)) {
			if (primaryConnection != null && primaryConnection.getRemoteHostString() == remoteHost) {
				senderChanged(null);
			}
			// stopSenderConnection(remoteHost);
		}
		OHUSenderConnection con = new OHUSenderConnection(localInetAddr);
		String uri = "";
		try {
			uri = con.run();
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
		OHUSenderSlave s = new OHUSenderSlave(mSlaves);
		OHUMessageSlave test = new OHUMessageSlave(s.getBuffer());
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
		this.primaryConnection = ohu;
		ohuSender.setOHUSenderConnector(ohu);
	}

	/***
	 * 
	 * @param con
	 */
	private void stopSender(OHUSenderConnection con) {
		con.stop();
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
