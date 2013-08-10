package org.rpi.plugin.lcddisplay;

import java.util.Observable;
import java.util.Observer;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Shutdown;

import org.apache.log4j.Logger;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventMuteChanged;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.player.events.EventVolumeChanged;
import org.rpi.playlist.CustomTrack;
import org.rpi.playlist.EventStandbyChanged;
import org.rpi.playlist.PlayManager;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.Lcd;

@PluginImplementation
public class LCDDisplayImpl implements LCDDislayInterface, Observer {

	private static Logger log = Logger.getLogger(LCDDisplayImpl.class);
	private GpioController gpio = null;

	// provision gpio pin #02 as an input pin with its internal pull down
	// resistor enabled
	
	//private GpioPinDigitalInput myButton = null;
	//private GpioPinDigitalOutput myMuteLed = null;

	public final static int LCD_ROWS = 2;
    public final static int LCD_COLUMNS = 20;
    public final static int LCD_BITS = 4;
    private int lcdHandle = -1;

	public LCDDisplayImpl() {
		log.debug("Init LCDDisplayImpl");
		PlayManager.getInstance().observInfoEvents(this);
		PlayManager.getInstance().observVolumeEvents(this);
		PlayManager.getInstance().observTimeEvents(this);
		PlayManager.getInstance().observeProductEvents(this);
		initPi4J();
	}

	private void initPi4J() {
		try {

			gpio = GpioFactory.getInstance();
//			myMuteLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, // PIN
//																			// NUMBER
//					"MuteLED", // PIN FRIENDLY NAME (optional)
//					PinState.LOW); // PIN STARTUP STATE (optional)
//			myMuteLed.setShutdownOptions(true, PinState.LOW);

			// provision gpio pin #02 as an input pin with its internal pull
			// down resistor enabled
//			myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);
//
//			myButton.addListener(new GpioPinListenerDigital() {
//				@Override
//				public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
//					log.debug(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
//					if (event.getState() == PinState.HIGH) {
//						PlayManager.getInstance().toggleMute();
//					}
//				}
//
//			});
			
			// initialize LCD
			// initialize LCD
	        lcdHandle= Lcd.lcdInit(LCD_ROWS,     // number of row supported by LCD
	                                   LCD_COLUMNS,  // number of columns supported by LCD
	                                   LCD_BITS,     // number of bits used to communicate to LCD 
	                                   11,           // LCD RS pin
	                                   10,           // LCD strobe pin
	                                   0,            // LCD data bit 1
	                                   1,            // LCD data bit 2
	                                   2,            // LCD data bit 3
	                                   3,            // LCD data bit 4
	                                   0,            // LCD data bit 5 (set to 0 if using 4 bit communication)
	                                   0,            // LCD data bit 6 (set to 0 if using 4 bit communication)
	                                   0,            // LCD data bit 7 (set to 0 if using 4 bit communication)
	                                   0);           // LCD data bit 8 (set to 0 if using 4 bit communication)

	        // verify initialization
	        if (lcdHandle == -1) {
	            log.warn(" ==>> LCD INIT FAILED");
	        }
	        
	        // clear LCD
	        LCDClear();
	        LCDWrite("Welcome", 0);

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
				log.debug("TrackChanged: " + track.getArtist() + " : " + track.getMetadata());
			} else {
				log.debug("Track was NULL");
			}
			
			break;
		case EVENTUPDATETRACKMETATEXT:
			EventUpdateTrackMetaText et = (EventUpdateTrackMetaText) e;
			log.debug("Track Changed: " + et.getArtist() + " : " + et.getTitle());
			break;
		case EVENTVOLUMECHNANGED:
			EventVolumeChanged ev = (EventVolumeChanged) e;
			LCDWrite("Volume: "+ev.getVolume(), 0);
			break;
		case EVENTMUTECHANGED:
			EventMuteChanged em = (EventMuteChanged) e;
			log.debug("MuteStateChanged: " + em.isMute());
			if(em.isMute())
			{
				//Lcd.lcdPosition(lcdHandle, 5, 0);
				//Lcd.lcdPuts(lcdHandle, "Test", args)
				LCDWrite("Mute ON", 1);
				
			}
			else
			{
				LCDWrite("Mute OFF", 1);
			}
			
			
//			if (myMuteLed != null) {
//				if (em.isMute()) {
//					myMuteLed.high();
//				} else {
//					myMuteLed.low();
//				}
//			} else {
//				log.debug("LED WAS NULL");
//			}
			break;
		case EVENTSTANDBYCHANGED:
			EventStandbyChanged es = (EventStandbyChanged)e;
			if(es.isStandby())
			{
				LCDClear();
			}
			else
			{
				LCDWrite("Welcome", 0);
			}
			break;
			
		}
	}
	
	private void LCDClear()
	{
		if(lcdHandle !=-1)
		{
			Lcd.lcdClear(lcdHandle);
		}
	}
	
	private void LCDWrite(String s, int Row)
	{
		if(lcdHandle !=-1)
		{
			Lcd.lcdPosition(lcdHandle, 0, Row);
			Lcd.lcdPuts(lcdHandle, padString(s, LCD_COLUMNS));
		}
	}
	
	private String padString(String in, int length)
	{
		String s = in;
		while(s.length() < length)
		{
			s += " ";
		}
		if(s.length()> length)
		{
			s = s.substring(0, length);
		}
		return s;
	}

	@Shutdown
	public void bye() {
		log.debug("ShutDown Called");
		if(lcdHandle !=-1)
		{
			Lcd.lcdClear(lcdHandle);
		}
		gpio.shutdown();
	}
}
