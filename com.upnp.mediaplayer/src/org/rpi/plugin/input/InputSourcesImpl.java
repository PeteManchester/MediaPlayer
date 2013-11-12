package org.rpi.plugin.input;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.rpi.os.OSManager;
import org.rpi.player.events.EventSourceChanged;
import org.rpi.plugingateway.PluginGateWay;
import org.rpi.sources.Source;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

@PluginImplementation
public class InputSourcesImpl implements InputSourcesInterface, Observer {

	private Logger log = Logger.getLogger(this.getClass());
	private ConcurrentHashMap<String, Source> sources = new ConcurrentHashMap<String, Source>();
	private String default_pin = "";
	private GpioController gpio = null;
	private GpioPinDigitalOutput myLed = null;

	public InputSourcesImpl() {
		// getSources();
		initPi4J();
		sources = PluginGateWay.getInstance().getSources();
		PluginGateWay.getInstance().addObserver(this);
	}

	private void initPi4J() {
		try {
			try {
				gpio = OSManager.getInstance().getGpio();
				if (null == gpio)
					throw new IllegalArgumentException("GPIO Not Initialized");
				log.debug("Setting up Pins");
				myLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, // PIN
																			// NUMBER
						"My LED", // PIN FRIENDLY NAME (optional)
						PinState.LOW); // PIN STARTUP STATE (optional)
				log.debug("Pins Set");
			} catch (Exception e) {
				log.error("Error Initialing pi4j", e);
			}
			log.info("Finished Configuring pi4j");
		} catch (Exception e) {
			log.error("Error Initializing Pi4J" + e.getMessage(), e);
		}
	}

	@Override
	public void update(Observable o, Object event) {
		if (event instanceof EventSourceChanged) {
			EventSourceChanged es = (EventSourceChanged) event;
			String name = es.getName();
			log.debug("Source Changed: " + name);
			if (sources.containsKey(name)) {
				try
				{
				Source source = sources.get(name);
				log.debug("Source PIN: " + source.getGPIO_PIN());
				String pin = source.getGPIO_PIN();
				if (name.equalsIgnoreCase("Home PC")) {
					log.debug("Taking Pin 6 High");
					if (null != myLed)
						myLed.high();
				} else {
					clearLEDS();
				}
				log.debug("Change Input to PIN: " + pin);
				}
				catch(Exception e)
				{
					log.error("Error Setting Pins: " + e);
				}
			} else {
				log.debug("Not A CustomSource Change Input to PIN: " + default_pin);
			}
		}

	}

	private void clearLEDS() {
		if (gpio != null) {
			myLed.low();
		}
	}

}
