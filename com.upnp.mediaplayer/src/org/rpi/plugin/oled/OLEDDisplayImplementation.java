package org.rpi.plugin.oled;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelBase;
import org.rpi.channel.ChannelPlayList;
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
import org.rpi.plugin.oled.transport.MockTransport;
import org.rpi.plugin.oled.transport.Transport;
import org.rpi.plugingateway.PluginGateWay;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class OLEDDisplayImplementation implements OLEDDisplayInterface, Observer {

	private Logger log = Logger.getLogger(OLEDDisplayImplementation.class);
	private Graphics graphics = null;
	private GpioController gpio = null;
	private long previousVolume = 0;

	private boolean isStandby = false;
	private Timer timer = new Timer("ClockTimer");

	public OLEDDisplayImplementation() {
		log.info("Init OLEDDisplayImpl");
		
		timer.scheduleAtFixedRate(new TimerTask() {
	        public void run() {	
	        	if(isStandby) {
	        		//graphics.setTime(""+Calendar.getInstance().getTime().toGMTString());
	        		LocalTime time = LocalTime.now();
	        		String t = time.format(DateTimeFormatter.ofPattern("HH:mm"));
	        		graphics.showMessage(t, 0, new Font("Arial", Font.PLAIN, 50));
	        	}	        	
	        }
	    }, 1000L, 1000L);

		Transport transport = null;

		try {
			initPi4J();
			transport = new I2CTransport(RaspiPin.GPIO_15, I2CBus.BUS_1, 0x3D);
		} catch (Exception e) {
			log.error("Error Init Pi4J: " + e);
			transport = new MockTransport();
		}

		// Transport
		// Or:
		// Transport transport = new SPITransport(SpiChannel.CS0,
		// RaspiPin.GPIO_15, RaspiPin.GPIO_16);

		SSD1306 ssd1306 = new SSD1306(128, 64, transport);
		graphics = ssd1306.getGraphics();
		graphics.clear();
		// graphics.drawStringFont("Hello", 0, 0, new Font("Arial", Font.PLAIN,
		// 50));

		PlayManager.getInstance().observeInfoEvents(this);
		PlayManager.getInstance().observeVolumeEvents(this);
		PlayManager.getInstance().observeTimeEvents(this);
		PlayManager.getInstance().observeProductEvents(this);
		PluginGateWay.getInstance().addObserver(this);

	}

	/***
	 * Initialisa Pi4J
	 * 
	 * @throws Exception
	 */
	private void initPi4J() throws Exception {
		gpio = OSManager.getInstance().getGpio();
		if (null == gpio) {
			throw new IllegalArgumentException("GPIO Not Initialized");
		}
		log.info("Finished Configuring pi4j");

	}

	@Override
	public void update(Observable o, Object e) {
		EventBase base = (EventBase) e;
		// log.debug(e);
		switch (base.getType()) {
		case EVENTTRACKCHANGED:
			try {
				EventTrackChanged etc = (EventTrackChanged) e;
				ChannelBase track = etc.getTrack();
				long duration = 0;
				if (track instanceof ChannelPlayList) {
					duration = track.getDuration();
				}
				if (track != null) {
					String text = track.getFullDetails();
					log.debug("TrackChanged: " + text);
					graphics.clear();
					graphics.setTitle(text, 0, 0, new Font("Arial", Font.PLAIN, 50));
					if (duration > 0) {
						duration = duration / 1000;
					}
					graphics.setDuration(ConvertTime(duration));
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
				String text = et.getTitle() + " : " + et.getArtist();
				if (et.getTitle().equals(et.getArtist())) {
					text = et.getTitle();
				}
				if (text.trim().equalsIgnoreCase("http://radiomonitor.com :")) {
					return;
				}
				graphics.setTitle(text, 0, 0, new Font("Arial", Font.ITALIC, 50));
				log.info("TrackMetaText Changed FINISHED: " + et.getTitle() + " : " + et.getArtist());
			} catch (Exception ex) {
				log.error("UpdateMetaData", ex);
			}
			break;
		case EVENTVOLUMECHANGED:
			try {
				EventVolumeChanged ev = (EventVolumeChanged) e;
				log.debug("Volume Changed: " + ev.getVolume());
				String text = "" + ev.getVolume();
				if (previousVolume != ev.getVolume()) {
					graphics.setVolume(text);
					graphics.showMessage(text, 3, new Font("Arial", Font.PLAIN, 50));
				}
			} catch (Exception ex) {
				log.error("VolumeChanged", ex);
			}
			break;
		case EVENTMUTECHANGED:
			if (o instanceof ObservableVolume) {
				try {
					EventMuteChanged em = (EventMuteChanged) e;
					log.debug("MuteStateChanged: " + em.isMute());
					if (em.isMute()) {
						log.debug("Muted");
						graphics.showMessage("Mute", 3, new Font("Arial", Font.PLAIN, 50));
					}
				} catch (Exception ex) {
					log.error("MuteChanged", ex);
				}
			}
			break;
		case EVENTSTANDBYCHANGED:
			try {
				EventStandbyChanged es = (EventStandbyChanged) e;
				log.debug("Standby Changed: " + es.isStandby());
				setStandby(es.isStandby());
			} catch (Exception ex) {
				log.error("StandbyChanged", ex);
			}
			break;
		case EVENTTIMEUPDATED:
			try {
				EventTimeUpdate etime = (EventTimeUpdate) e;
				graphics.setTime(ConvertTime(etime.getTime()));
			} catch (Exception ex) {
				log.error("TimeUpdated", ex);
			}
			break;

		case EVENTDISPOSING:
			try {
				log.info("Disposing");
				graphics.dispose();
			} catch (Exception ex) {

			}
			break;

		}

	}

	/***
	 * Convert seconds to Hours:Seconds
	 * 
	 * @param lTime
	 * @return
	 */
	private String ConvertTime(long lTime) {
		if (lTime == 0)
			return "0:00";
		try {
			if (lTime <= Integer.MAX_VALUE) {
				int minutes = (int) lTime / 60;
				int seconds = (int) lTime % 60;
				String sSeconds = "";
				if (seconds < 10) {
					sSeconds = "0" + seconds;
				} else {
					sSeconds = "" + seconds;
				}
				return "" + minutes + ":" + sSeconds;
			}
		} catch (Exception e) {

		}
		return "" + lTime;
	}

	private void setStandby(boolean isStandby) {
		this.isStandby = isStandby;
		try {
			if (isStandby) {
				graphics.setScroll(false);
				graphics.showMessage("Bye", 3, new Font("Arial", Font.PLAIN, 50));
				graphics.dimContrast(50);
				Thread.sleep(1000);
				graphics.clear();
			} else {
				//TODO Move this to a thread so it does not block everything else
				graphics.setScroll(false);
				String path = OSManager.getInstance().getFilePath(this.getClass(), false);
				File img = new File(path + "mediaplayer240.jpg");
				BufferedImage image = ImageIO.read(img);
				graphics.image(image, 0, 0, 128, 64);
				graphics.brightenContrast(255);
				graphics.dimContrast(0);
				graphics.brightenContrast(255);
				graphics.clear();
			}
			
		} catch (Exception e) {
			log.error(e);
		}

	}

}
