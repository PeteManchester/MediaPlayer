package org.rpi.songcast.ohu.sender;

import java.time.Instant;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelBase;
import org.rpi.config.Config;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.songcast.ohu.sender.mpd.MPDStreamerController;
import org.rpi.songcast.ohu.sender.response.OHUSenderAudioResponse;
import org.rpi.songcast.ohu.sender.response.OHUSenderMetaTextResponse;
import org.rpi.songcast.ohu.sender.response.OHUSenderTrackResponse;

import io.netty.buffer.ByteBuf;

public class OHUSenderThread implements Runnable, Observer {

	private boolean isRun = true;
	private OHUSenderConnection ohu = null;

	private int iMetaTextSequence = 0;
	private int iTrackSequence = 0;
	private int iCount = 0;
	//private long iSendTime = 0;
	private int iCountNoFrame = 0;
	private int iLatency = 0;

	private Logger log = Logger.getLogger(this.getClass());
	
	private long audioLength = 10;

	/***
	 * 
	 * @param ohu
	 */
	public OHUSenderThread(OHUSenderConnection ohu) {
		setOHUSenderConnector(ohu);
		iLatency = Config.getInstance().getSongcastLatency();
		PlayManager.getInstance().observeInfoEvents(this);
	}

	/***
	 * Set the OHUSenderConnection
	 * 
	 * @param ohu
	 */
	public void setOHUSenderConnector(OHUSenderConnection ohu) {
		this.ohu = ohu;
	}

	public void run() {
		try {
			while (isRun) {
				// Time how long it takes to create and send the message
				long now = System.nanoTime();
				ByteBuf b = MPDStreamerController.getInstance().getNext();
				if (b != null) {
					if (ohu != null) {
						try {
							OHUSenderAudioResponse tab = new OHUSenderAudioResponse(b, iCount, iLatency);
							if(audioLength != tab.getAudioLength()) {
								audioLength = tab.getAudioLength();
								log.debug("Audio Length: " + audioLength);
							}
							ohu.sendMessage(tab);
							
							/*
							if (iCount % 1000 == 0) {
								log.debug("Count: " + iCount + " Longest SendTime: " + iSendTime + " NoFrameCount: " + iCountNoFrame);
								iSendTime = 0;
								iCountNoFrame = 0;
							}
							*/
							//.log.debug("SendTime: " + seconds);
							/*
							long delay = 10000000 - (seconds + 4000000);
							if (delay < 1) {
								log.debug("Delay was less than one! " + seconds);
								delay = 1;
							}
							*/
							//log.debug("SendTime: " + seconds + " Delay: " + delay);
							//TimeUnit.NANOSECONDS.sleep(delay);
							//TimeUnit.MICROSECONDS.sleep((8200));
							
							Instant.now();
							
							long nanoSeconds = System.nanoTime() - now;
							iCount++;
							/*
							if (iSendTime < nanoSeconds) {
								iSendTime = nanoSeconds;
							}
							*/
							
							long delay = (audioLength - (nanoSeconds/1000)) - 1500;
							long pcAudioLength = (audioLength/60) * 100;
							//Too short a delay increases the risk of dropping packets..
							if(delay < audioLength - pcAudioLength) {
								log.debug("Delay was too small: " + delay);
								delay = pcAudioLength;
							}
							//log.debug(delay);
							TimeUnit.MICROSECONDS.sleep(delay);
						} catch (Exception e) {
							log.error("Error Send AudioBytes", e);
						}
					}
				} else {
					iCountNoFrame++;
					TimeUnit.MILLISECONDS.sleep(2);
				}
			}
		} catch (Exception e) {
			log.error("Error Get AudioBytes", e);
		}

	}

	/**
	 * Stop the Thread running
	 */
	public void stop() {
		audioLength = 0;
		isRun = false;
	}

	/***
	 * Update Event Received
	 */
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
				OHUSenderTrackResponse r = new OHUSenderTrackResponse(iTrackSequence, track.getUri(), track.getMetadata());
				ohu.sendMessage(r);
				iTrackSequence++;
				if (iTrackSequence >= Integer.MAX_VALUE) {
					iTrackSequence = 0;
				}
			} catch (Exception ex) {
				log.error("Track Changed", ex);
			}

			break;
		case EVENTUPDATETRACKMETATEXT:
			try {
				if (ohu == null) {
					return;
				}
				EventUpdateTrackMetaText et = (EventUpdateTrackMetaText) e;
				OHUSenderMetaTextResponse mtr = new OHUSenderMetaTextResponse(iMetaTextSequence, et.getMetaText());
				ohu.sendMessage(mtr);
				iMetaTextSequence++;
				if (iMetaTextSequence >= Integer.MAX_VALUE) {
					iMetaTextSequence = 0;
				}
				log.debug("TrackMetaDataChanged: " + et.getMetaText());
			} catch (Exception ex) {
				log.error("MetaText Changed", ex);
			}
		}

	}

}
