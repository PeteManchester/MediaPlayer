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

@PluginImplementation
public class LCDDisplayImpl implements LCDDislayInterface, Observer {

	private static Logger log = Logger.getLogger(LCDDisplayImpl.class);
	private GpioController gpio = null;

	// provision gpio pin #02 as an input pin with its internal pull down
	// resistor enabled
	private GpioPinDigitalInput myButton = null;
	private GpioPinDigitalOutput myMuteLed = null;

	public LCDDisplayImpl() {
		log.debug("Init LCDDisplayImpl");
		PlayManager.getInstance().observInfoEvents(this);
		PlayManager.getInstance().observVolumeEvents(this);
		PlayManager.getInstance().observTimeEvents(this);
		initPi4J();
	}

	private void initPi4J() {
		try {

			gpio = GpioFactory.getInstance();
			myMuteLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, // PIN
																			// NUMBER
					"MuteLED", // PIN FRIENDLY NAME (optional)
					PinState.LOW); // PIN STARTUP STATE (optional)
			myMuteLed.setShutdownOptions(true, PinState.LOW);
			
			// provision gpio pin #02 as an input pin with its internal pull down resistor enabled
	        myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);
	        
	        myButton.addListener(new GpioPinListenerDigital() {
	            @Override
	            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
	                log.debug(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
	                if(event.getState() == PinState.HIGH)
	                {
	                	PlayManager.getInstance().toggleMute();
	                }
	            }
	            
	        });

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
			log.debug("VolumeChanged: " + ev.getVolume());
			break;
		case EVENTMUTECHANGED:
			EventMuteChanged em = (EventMuteChanged) e;
			log.debug("MuteStateChanged: " + em.isMute());
			if (myMuteLed != null) {
				if (em.isMute()) {
					myMuteLed.high();
				} else {
					myMuteLed.low();
				}
			} else {
				log.debug("LED WAS NULL");
			}
			break;
		}
	}
	
	@Shutdown
    public void bye() {
		log.debug("ShutDown Called");
        gpio.shutdown();
    }
}
