package org.rpi.providers;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgReceiver1;
import org.rpi.channel.ChannelPlayList;
import org.rpi.channel.ChannelSongcast;
import org.rpi.config.Config;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventPlayListStatusChanged;
import org.rpi.songcast.ohz.OHZManager;
import org.rpi.utils.Utils;

public class PrvReceiver extends DvProviderAvOpenhomeOrgReceiver1 implements IDisposableDevice, Observer {

	private Logger log = Logger.getLogger(PrvReceiver.class);
	private boolean bPlay = false;
	private ChannelPlayList track = null;
	private OHZManager manager = null;
	private String zoneID = "";

	public PrvReceiver(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating Template");
		enablePropertyMetadata();
		enablePropertyProtocolInfo();
		enablePropertyTransportState();
		enablePropertyUri();

		setPropertyMetadata("");
		setPropertyProtocolInfo(Config.getProtocolInfo());
		setPropertyTransportState("Stopped");
		setPropertyUri("");

		enableActionPlay();
		enableActionProtocolInfo();
		enableActionSender();
		enableActionSetSender();
		enableActionStop();
		enableActionTransportState();
		PlayManager.getInstance().observeSongcastEvents(this);
	}

	@Override
	protected String protocolInfo(IDvInvocation paramIDvInvocation) {
		log.debug("getProtocolInfo" + Utils.getLogText(paramIDvInvocation));
		return getPropertyProtocolInfo();
	}

	@Override
	protected Sender sender(IDvInvocation paramIDvInvocation) {
		log.debug("Sender" + Utils.getLogText(paramIDvInvocation));
		Sender sender = new Sender("", "");
		// TODO Auto-generated method stub
		return sender;
	}

	@Override
	protected void play(IDvInvocation paramIDvInvocation) {
		log.debug("Play" + Utils.getLogText(paramIDvInvocation));
		bPlay = true;
		// Play seems to come before setSender so set a boolean flag that we
		// want to play and then when we
		// get the setSender event start playing??
	}

	@Override
	protected void setSender(IDvInvocation paramIDvInvocation, String uri, String metadata) {
		log.debug("SetSender, URL: " + uri + " MetaData: " + metadata + Utils.getLogText(paramIDvInvocation));
		propertiesLock();
		setPropertyUri(uri);
		propertiesUnlock();
		setPropertyMetadata(metadata);

		if (bPlay = true) {
			String ip = Utils.getAdapterIP(paramIDvInvocation.getAdapter());
			String nic = "";
			ChannelSongcast track = new ChannelSongcast(uri, metadata, 1);
			try {
				InetAddress local_address = InetAddress.getByName(ip);

				Enumeration e = NetworkInterface.getNetworkInterfaces();
				while (e.hasMoreElements()) {
					NetworkInterface n = (NetworkInterface) e.nextElement();
					Enumeration ee = n.getInetAddresses();
					//log.info("Network Interface Display Name: '" + n.getDisplayName() + "'");
					//log.info("NIC Name: '" + n.getName() + "'");
					while (ee.hasMoreElements()) {
						InetAddress i = (InetAddress) ee.nextElement();
						if (i.getHostAddress().equalsIgnoreCase(ip)) {
							log.info("IPAddress for Network Interface: " + n.getDisplayName() + " : " + i.getHostAddress());
							nic = n.getName();
							Config.songcastNICName = nic;
						}
					}
				}

				PlayManager.getInstance().playSongcast(track);
				// String nic = Config.songcastNICName;
				// if (nic.equalsIgnoreCase("")) {
				// log.error("No NIC Configured for SONGCAST");
				// return;
				// }
				if (manager != null) {
					manager.stop(zoneID);
					manager = null;
				}
				int lastSlash = uri.lastIndexOf("/");
				String songcast_url = uri.substring(0, lastSlash);
				zoneID = uri.substring(lastSlash + 1);
				log.debug("SongCast URL: " + songcast_url + " ZoneID: " + zoneID);

				manager = new OHZManager(songcast_url, zoneID, nic);
				manager.start();
			} catch (Exception e) {
				log.error("Error Starting Songcast Playback: ", e);

			}

		}
	}

	@Override
	protected void stop(IDvInvocation paramIDvInvocation) {
		log.debug("Stop" + Utils.getLogText(paramIDvInvocation));
		stop();
	}

	private void stop() {
		manager.stop(zoneID);
		manager = null;
		PlayManager.getInstance().setStatus("Stopped");
	}

	@Override
	protected String transportState(IDvInvocation paramIDvInvocation) {
		log.debug("Transport State" + Utils.getLogText(paramIDvInvocation));
		return getPropertyTransportState();
	}

	@Override
	public String getName() {
		return "Receiver";
	}

	@Override
	public void update(Observable arg0, Object ev) {
		EventBase e = (EventBase) ev;
		switch (e.getType()) {
		case EVENTPLAYLISTSTATUSCHANGED:
			EventPlayListStatusChanged ers = (EventPlayListStatusChanged) e;
			setStatus(ers.getStatus());
			break;
		case EVENTSTOPSONGCAST:
			stop();
			break;
		}

	}

	public void setStatus(String status) {
		setPropertyTransportState(status);
	}

}