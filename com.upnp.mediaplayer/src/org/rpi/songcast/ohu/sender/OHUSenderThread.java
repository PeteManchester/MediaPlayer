package org.rpi.songcast.ohu.sender;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelBase;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.songcast.ohu.sender.mpd.MPDStreamerController;
import org.rpi.songcast.ohu.sender.response.OHUSenderAudioResponse;
import org.rpi.songcast.ohu.sender.response.OHUSenderTrackResponse;

public class OHUSenderThread implements Runnable, Observer {

	private boolean isRun = true;
	private OHUSenderConnection ohu = null;

	private Logger log = Logger.getLogger(this.getClass());

	public OHUSenderThread(OHUSenderConnection ohu) {
		setOHUSenderConnector(ohu);
		PlayManager.getInstance().observeInfoEvents(this);
	}

	public void setOHUSenderConnector(OHUSenderConnection ohu) {
		this.ohu = ohu;
	}

	@Override
	public void run() {
		try {
			while (isRun) {

				OHUSenderAudioResponse tab = MPDStreamerController.getInstance().getNext();
				if (tab != null) {
					if (ohu != null) {
						try {
							ohu.sendMessage(tab);
							TimeUnit.MILLISECONDS.sleep(1);
						} catch (Exception e) {
							log.error("Error Send AudioBytes", e);
						}
					}
				} else {
					TimeUnit.MILLISECONDS.sleep(10);
				}
			}
		} catch (Exception e) {
			log.error("Error Get AudioBytes", e);
		}
	}

	public void stop() {
		isRun = false;
	}

	@Override
	public void update(Observable o, Object e) {
		EventBase base = (EventBase) e;
		switch (base.getType()) {
		case EVENTTRACKCHANGED:
			try {
				if (ohu == null) {
					return;
				}
				EventTrackChanged etc = (EventTrackChanged) e;
				ChannelBase track = etc.getTrack();
				OHUSenderTrackResponse r = new OHUSenderTrackResponse(1, track.getUri(), track.getMetadata());
				ohu.sendMessage(r);
			} catch (Exception ex) {
				log.error("TrackChanged", ex);
			}

			break;
		case EVENTUPDATETRACKMETATEXT:
			EventUpdateTrackMetaText et = (EventUpdateTrackMetaText) e;
			log.debug("TrackMetaDataChanged: " + et.getMetaText());
		}

	}

}
