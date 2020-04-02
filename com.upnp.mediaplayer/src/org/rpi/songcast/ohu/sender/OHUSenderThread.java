package org.rpi.songcast.ohu.sender;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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
import org.rpi.songcast.ohu.sender.response.OHUSenderMetaTextResponse;
import org.rpi.songcast.ohu.sender.response.OHUSenderTrackResponse;

import io.netty.buffer.ByteBuf;

public class OHUSenderThread implements Runnable, Observer {

	private boolean isRun = true;
	private OHUSenderConnection ohu = null;
	
	private int iMetaTextSequence = 0;
	private int iTrackSequence = 0;
	private int iCount = 0;
	private int iSendTime = 0;
	private int iCountNameFrame = 0;

	private Logger log = Logger.getLogger(this.getClass());

	public OHUSenderThread(OHUSenderConnection ohu) {
		setOHUSenderConnector(ohu);
		PlayManager.getInstance().observeInfoEvents(this);
	}

	public void setOHUSenderConnector(OHUSenderConnection ohu) {
		this.ohu = ohu;
	}
	
	
	public void run() {
		
		try {
			while (isRun) {
				ZonedDateTime now = ZonedDateTime.now();	
				
				ByteBuf b = MPDStreamerController.getInstance().getNext();				
				if (b != null) {
					if (ohu != null) {
						try {
													
							OHUSenderAudioResponse tab = new OHUSenderAudioResponse(b,iCount);
							ohu.sendMessage(tab);
							int seconds = (int) now.until(ZonedDateTime.now(), ChronoUnit.MILLIS);
							iCount++;
							if(iSendTime < seconds) {
								iSendTime = seconds;
							}
							if(iCount % 1000 ==0) {
								log.debug("Count: " + iCount + " Longest SendTime: " + iSendTime + " NoFrameCount: " + iCountNameFrame);
								iSendTime = 0;
								iCountNameFrame = 0;
							}
							TimeUnit.MILLISECONDS.sleep(2);
						} catch (Exception e) {
							log.error("Error Send AudioBytes", e);
						}
					}
				} else {
					//log.debug("No Frame");
					iCountNameFrame++;
					TimeUnit.MILLISECONDS.sleep(2);					
				}
			}
		} catch (Exception e) {
			log.error("Error Get AudioBytes", e);
		}


		
	}

	/*
	@Override
	public void run() {
		try {
			while (isRun) {

				OHUSenderAudioResponse tab = MPDStreamerController.getInstance().getNext();
				if (tab != null) {
					if (ohu != null) {
						try {
							
							ZonedDateTime now = ZonedDateTime.now();							
							ohu.sendMessage(tab);
							int seconds = (int) now.until(ZonedDateTime.now(), ChronoUnit.MILLIS);
							iCount++;
							if(iSendTime < seconds) {
								iSendTime = seconds;
							}
							if(iCount % 1000 ==0) {
								log.debug("Longest SendTime: " + iSendTime);
								iSendTime = 0;
							}
							
							TimeUnit.MILLISECONDS.sleep(2);
						} catch (Exception e) {
							log.error("Error Send AudioBytes", e);
						}
					}
				} else {
					TimeUnit.MILLISECONDS.sleep(4);
				}
			}
		} catch (Exception e) {
			log.error("Error Get AudioBytes", e);
		}
	}
	*/

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
				OHUSenderTrackResponse r = new OHUSenderTrackResponse(iTrackSequence, track.getUri(), track.getMetadata());
				ohu.sendMessage(r);
				iTrackSequence++;
				if(iTrackSequence >= Integer.MAX_VALUE) {
					iTrackSequence = 0;
				}
			} catch (Exception ex) {
				log.error("Track Changed", ex);
			}

			break;
		case EVENTUPDATETRACKMETATEXT:
			try {
				if(ohu ==null) {
					return;
				}				
				EventUpdateTrackMetaText et = (EventUpdateTrackMetaText) e;
				OHUSenderMetaTextResponse mtr = new OHUSenderMetaTextResponse(iMetaTextSequence, et.getMetaText());
				ohu.sendMessage(mtr);
				iMetaTextSequence++;
				if(iMetaTextSequence >= Integer.MAX_VALUE) {
					iMetaTextSequence = 0;
				}
				log.debug("TrackMetaDataChanged: " + et.getMetaText());
			}
			catch(Exception ex) {
				log.error("MetaText Changed", ex);
			}
			
		}
	
	}

}
