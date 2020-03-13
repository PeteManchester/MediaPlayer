package org.rpi.songcast.ohu.sender;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.sender.mpd.MPDStreamerController;
import org.rpi.songcast.ohu.sender.response.OHUSenderAudioResponse;

public class OHUSenderThread implements Runnable {

	private boolean isRun = true;
	private OHUSenderConnection ohu = null;

	private Logger log = Logger.getLogger(this.getClass());

	public OHUSenderThread(OHUSenderConnection ohu) {
		setOHUSenderConnector(ohu);
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
							// TimeUnit.MILLISECONDS.sleep(2);
						} catch (Exception e) {
							log.error("Error Send AudioBytes", e);
						}
					}
				} else {
					TimeUnit.MILLISECONDS.sleep(20);
				}
			}
		} catch (Exception e) {
			log.error("Error Get AudioBytes", e);
		}
	}

	public void stop() {
		isRun = false;
	}

}
