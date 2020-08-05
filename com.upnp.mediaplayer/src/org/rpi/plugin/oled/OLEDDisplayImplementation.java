package org.rpi.plugin.oled;

import java.awt.Font;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelBase;
import org.rpi.os.OSManager;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventMuteChanged;
import org.rpi.player.events.EventStandbyChanged;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.player.events.EventVolumeChanged;
import org.rpi.player.observers.ObservableVolume;
import org.rpi.plugin.oled.transport.I2CTransport;
import org.rpi.plugin.oled.transport.Transport;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class OLEDDisplayImplementation implements OLEDDisplayInterface, Observer {
	
	private Logger log = Logger.getLogger(OLEDDisplayImplementation.class);
	private Graphics graphics = null;
	private GpioController gpio = null;
	
	public OLEDDisplayImplementation() {
		log.info("Init OLEDDisplayImpl");	
		initPi4J();
		Transport transport = null;
		transport = new I2CTransport(RaspiPin.GPIO_15, I2CBus.BUS_1, 0x3D);	
		
		//Transport 
		// Or:
		//Transport transport = new SPITransport(SpiChannel.CS0, RaspiPin.GPIO_15, RaspiPin.GPIO_16);

		SSD1306 ssd1306 = new SSD1306(128, 64, transport);
		graphics = ssd1306.getGraphics();
		
		graphics.drawStringFont("Hello", 0, 0, new Font("Arial", Font.PLAIN, 50));
		
		PlayManager.getInstance().observeInfoEvents(this);
		PlayManager.getInstance().observeVolumeEvents(this);
		PlayManager.getInstance().observeTimeEvents(this);
		PlayManager.getInstance().observeProductEvents(this);
	}
	
	private void initPi4J() {
		try {
			gpio = OSManager.getInstance().getGpio();
			if (null == gpio)
				throw new IllegalArgumentException("GPIO Not Initialized");
			log.info("Finished Configuring pi4j");
		} catch (Exception e) {
			log.error("Error Initializing Pi4J" + e.getMessage());
		}
	}
	
	
	@Override
	public void update(Observable o, Object e) {
		EventBase base = (EventBase) e;
		switch (base.getType()) {
		case EVENTTRACKCHANGED:
			try {
				EventTrackChanged etc = (EventTrackChanged) e;
				ChannelBase track = etc.getTrack();
				if (track != null) {
					String text =  track.getFullDetails();
					log.debug("TrackChanged: " + text);
					graphics.scrollerMyText(text, 0, 0,new Font("Arial", Font.PLAIN, 50));
					// UpdateScroller(s, 0);
					/*
					scroller.updateValues("[FULL_DETAILS]", s);
					scroller.updateValues("[ARTIST]", track.getArtist());
					scroller.updateValues("[TITLE]", track.getTitle());
					scroller.updateValues("[ALBUM]", track.getAlbum());
					scroller.updateValues("[PERFORMER]", track.getPerformer());
					scroller.updateValues("[COMPOSER]", track.getComposer());
					scroller.updateValues("[CONDUCTOR]", track.getConductor());
					scroller.updateValues("[DATE]", track.getDate());
					scroller.updateValues("[STANDBY]", "");
					*/
				} else {
					log.debug("Track was NULL");
				}
			} catch (Exception ex) {
				log.error("TrackChanged", ex);
			}

			break;
		case EVENTUPDATETRACKMETATEXT:
			EventUpdateTrackMetaText et = (EventUpdateTrackMetaText) e;
			try {
				log.debug("TrackMetaText Changed: " + et.getTitle() + " : " + et.getArtist());
				
				String text =  et.getTitle() + " : " + et.getArtist();
				
				if(et.getTitle().equals(et.getArtist())) {
					text = et.getTitle();
				}	
				
				
				if(text.trim().equalsIgnoreCase("http://radiomonitor.com :")) {
					return;
				}
				
				graphics.scrollerMyText(text, 0, 0,new Font("Arial", Font.ITALIC, 50));
				/*
				if (scroller != null) {
					// UpdateScroller(et.getTitle() + " - " + et.getArtist(),
					// 0);
					scroller.updateValues("[TITLE]", et.getTitle());
					scroller.updateValues("[ARTIST]", et.getArtist());
					scroller.updateValues("[PERFORMER]", et.getArtist());
				}
				*/
			} catch (Exception ex) {
				log.error("UpdateMetaData", ex);
			}
			break;
		case EVENTVOLUMECHANGED:
			try {
				EventVolumeChanged ev = (EventVolumeChanged) e;
				log.debug("Volume Changed: " + ev.getVolume());
				String text =  ""+ ev.getVolume();
				graphics.pauseScroller(10);
				graphics.clear();
				graphics.drawStringFont(text, 0, 0,new Font("Arial", Font.PLAIN, 50));
				/*
				mVolume = ev.getVolume();
				// updateVolume();
				if(scroller !=null)
				{
					scroller.updateValues("[VOLUME]", "" + mVolume);
				}
				*/
			} catch (Exception ex) {
				log.error("VolumeChanged", ex);
			}
			break;
		case EVENTMUTECHANGED:
			if (e instanceof ObservableVolume) {
				try {
					EventMuteChanged em = (EventMuteChanged) e;
					log.debug("MuteStateChanged: " + em.isMute());
					/*
					isMute = em.isMute();
					if (em.isMute()) {
						if(scroller !=null)
						{
							scroller.updateValues("[VOLUME]", "Mute");
						}
					} else {
						if(scroller !=null)
						{
							scroller.updateValues("[VOLUME]", "" + mVolume);
						}
					}
					*/
				} catch (Exception ex) {
					log.error("MuteChanged", ex);
				}
			}
			break;
		case EVENTSTANDBYCHANGED:
			try {
				EventStandbyChanged es = (EventStandbyChanged) e;
				log.debug("Standby Changed: " + es.isStandby());
				if(es.isStandby()) {
					String text =  "Goodbye";
					graphics.stopScroller();
					graphics.drawStringFont(text, 0, 0,new Font("Arial", Font.PLAIN, 50));
					graphics.dimContrast(50);
					Thread.sleep(1000);
					graphics.clear();
				}
				else {
					String text =  "Hello";	
					graphics.stopScroller();
					graphics.drawStringFont(text, 0, 0,new Font("Arial", Font.PLAIN, 50));
					graphics.brightenContrast(255);
				}
				
			} catch (Exception ex) {
				log.error("StandbyChanged", ex);
			}
			break;
		case EVENTTIMEUPDATED:
			try {
				EventTimeUpdate etime = (EventTimeUpdate) e;
				/*
				if (mTime != null) {
					mTime = ConvertTime(etime.getTime());
				}
				// updateVolume();
				if(scroller !=null)
				{
				scroller.updateValues("[TIME]", mTime);
				}
				*/
			} catch (Exception ex) {
				log.error("TimeUpdated", ex);
			}
			break;

		}
		
	}

}
