package org.rpi.plugin.lcddisplay;

import java.util.Observable;
import java.util.Observer;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Shutdown;

import org.apache.log4j.Logger;
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
import org.rpi.playlist.CustomTrack;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.wiringpi.Lcd;

@PluginImplementation
public class LCDDisplayImpl implements LCDDislayInterface, Observer {

	private static Logger log = Logger.getLogger(LCDDisplayImpl.class);
	private GpioController gpio = null;

	// provision gpio pin #02 as an input pin with its internal pull down
	// resistor enabled

	// private GpioPinDigitalInput myButton = null;
	// private GpioPinDigitalOutput myMuteLed = null;

	public final static int LCD_ROWS = 2;
	public final static int LCD_COLUMNS = 20;
	public final static int LCD_BITS = 4;
	private int lcdHandle = -1;
	private long mVolume = 100;
	private String mTime = "0:00";
	private boolean isMute = false;
	private LCDScroller scroller = null;

	public LCDDisplayImpl() {
		log.debug("Init LCDDisplayImpl");
		try {
			PlayManager.getInstance().observInfoEvents(this);
			PlayManager.getInstance().observVolumeEvents(this);
			PlayManager.getInstance().observTimeEvents(this);
			PlayManager.getInstance().observeProductEvents(this);
			try {
				initPi4J();
			} catch (Exception e) {
				log.error("Error Init Pi4J: " + e);
			}
			scroller = new LCDScroller();
			if (lcdHandle != -1) {
				scroller.setLCDHandle(lcdHandle);
				scroller.start();
				//welcomeMessage();
			}
		} catch (Exception e) {
			log.error("Error Init LCDDisplayImpl", e);
		}
	}

	private void initPi4J() {
		try {

			//gpio = GpioFactory.getInstance();
			gpio = OSManager.getInstance().getGpio();
			if(null == gpio)
				throw new IllegalArgumentException("GPIO Not Initialized");

			lcdHandle = Lcd.lcdInit(LCD_ROWS, // number of row supported by LCD
					LCD_COLUMNS, // number of columns supported by LCD
					LCD_BITS, // number of bits used to communicate to LCD
					11, // LCD RS pin
					10, // LCD strobe pin
					0, // LCD data bit 1
					1, // LCD data bit 2
					2, // LCD data bit 3
					3, // LCD data bit 4
					0, // LCD data bit 5 (set to 0 if using 4 bit communication)
					0, // LCD data bit 6 (set to 0 if using 4 bit communication)
					0, // LCD data bit 7 (set to 0 if using 4 bit communication)
					0); // LCD data bit 8 (set to 0 if using 4 bit
						// communication)

			// verify initialization
			if (lcdHandle == -1) {
				log.warn(" ==>> LCD INIT FAILED");
			}

			// clear LCD
			//LCDClear();
			scroller.setReset();
			log.info("Finished Configuring LCD");
		} catch (Exception e) {
			log.error("Error Initializing Pi4J" + e.getMessage());
		}

	}

	@Override
	public void update(Observable o, Object e) {
		// log.debug("Event: " + e.toString());
		EventBase base = (EventBase) e;
		switch (base.getType()) {
		case EVENTTRACKCHANGED:
			EventTrackChanged etc = (EventTrackChanged) e;
			CustomTrack track = etc.getTrack();
			if (track != null) {
				String s = track.getFullDetails();
				log.debug("TrackChanged: " + s);
				UpdateScroller(s, 0);
			} else {
				log.debug("Track was NULL");
			}

			break;
		case EVENTUPDATETRACKMETATEXT:
			EventUpdateTrackMetaText et = (EventUpdateTrackMetaText) e;
			log.debug("Track Changed: " + et.getTitle() + " : " + et.getArtist());
			if (scroller != null) {
				UpdateScroller(et.getTitle() + " - " + et.getArtist(), 0);
			}
			break;
		case EVENTVOLUMECHNANGED:
			EventVolumeChanged ev = (EventVolumeChanged) e;
			mVolume = ev.getVolume();
			updateVolume();
			break;
		case EVENTMUTECHANGED:
			if (o instanceof ObservableVolume) {
				EventMuteChanged em = (EventMuteChanged) e;
				log.debug("MuteStateChanged: " + em.isMute());
				isMute = em.isMute();
			}
			break;
		case EVENTSTANDBYCHANGED:
			EventStandbyChanged es = (EventStandbyChanged) e;
			scroller.setReset();
			if (es.isStandby()) {
				scroller.setStandBy(true);
			} else {
				scroller.setStandBy(false);
				try {
				} catch (Exception ex) {

				}
			}
			break;
		case EVENTTIMEUPDATED:
			EventTimeUpdate etime = (EventTimeUpdate) e;
			mTime = ConvertTime(etime.getTime());
			updateVolume();
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

	/***
	 * Clear the LCD
	 */
//	private void LCDClear() {
//		if (lcdHandle != -1) {
//			Lcd.lcdClear(lcdHandle);
//		}
//	}

	/***
	 * Update the Volume
	 */
	private void updateVolume() {
		StringBuilder sb = new StringBuilder();
		if (isMute) {
			sb.append("Mute");
			sb.append(" ");
		} else {
			sb.append("Vol:" + mVolume);
			sb.append(" ");
		}
		sb.append("Time:" + mTime);
		// String text = "Vol:" + mVolume + " Time:" + mTime;
		UpdateScroller(sb.toString(), 1);
	}

	/***
	 * Update the Scroller Row Text
	 * 
	 * @param text
	 * @param row
	 */
	private void UpdateScroller(String text, int row) {
		try {
			if (scroller != null) {
				scroller.setText(text, row);
			}
		} catch (Exception e) {
			log.error("Error UpdateScroller: ", e);
		}
	}



	@Shutdown
	public void bye() {
		log.debug("ShutDown Called");
		if (scroller != null) {
			scroller = null;
		}

		if (lcdHandle != -1) {
			Lcd.lcdClear(lcdHandle);
		}
	}


}
