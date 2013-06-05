package org.rpi.plugin.lcddisplay;

import java.util.EventObject;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.rpi.player.IPlayerEventClassListener;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaData;
import org.rpi.playlist.CustomTrack;
import org.rpi.playlist.PlayManager;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

@PluginImplementation
public class LCDDisplayImpl implements LCDDislayInterface,
		IPlayerEventClassListener {

	private static Logger log = Logger.getLogger(LCDDisplayImpl.class);

	private GpioController gpio = null;

	// provision gpio pin #02 as an input pin with its internal pull down resistor enabled
	private GpioPinDigitalInput myButton = null;

	public LCDDisplayImpl() {
		log.debug("Init LCDDisplayImpl");
		PlayManager.getInstance().addEventListener(this);
		initPi4J();
	}

	private void initPi4J() {
		try {
			
			gpio = GpioFactory.getInstance();
			myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02,
					PinPullResistance.PULL_DOWN);
		} catch (Exception e) {
			log.error("Error Initializing Pi4J", e);
		}

	}

	public void addButtons() {
		try {
			// create and register gpio pin listener
			myButton.addListener(new GpioPinListenerDigital() {
				@Override
				public void handleGpioPinDigitalStateChangeEvent(
						GpioPinDigitalStateChangeEvent event) {
					// display pin state on console
					log.debug(" --> GPIO PIN STATE CHANGE: " + event.getPin()
							+ " = " + event.getState());
				}

			});
		} catch (Exception e) {

		}
	}

	@Override
	public void handleMyEventClassEvent(EventObject e) {
		log.debug("Event Received");
		if (e instanceof EventUpdateTrackMetaData) {
			EventUpdateTrackMetaData et = (EventUpdateTrackMetaData) e;
			log.debug("Track Changed: " + et.getArtist() + " : "
					+ et.getTitle());
		} else if (e instanceof EventTrackChanged) {
			EventTrackChanged etc = (EventTrackChanged) e;
			CustomTrack track = etc.getTrack();
			if (track != null) {
				log.debug("TrackChanged: " + track.getArtist() + " : "
						+ track.getMetadata());
			} else {
				log.debug("Track was NULL");
			}
		}
	}

}
