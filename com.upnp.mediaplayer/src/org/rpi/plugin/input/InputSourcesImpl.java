package org.rpi.plugin.input;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Shutdown;

import org.apache.log4j.Logger;
import org.rpi.os.OSManager;
import org.rpi.player.events.EventSourceChanged;
import org.rpi.plugingateway.PluginGateWay;
import org.rpi.sources.Source;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Lcd;

@PluginImplementation
public class InputSourcesImpl implements InputSourcesInterface, Observer {

	private Logger log = Logger.getLogger(this.getClass());
	private ConcurrentHashMap<String, Source> sources = new ConcurrentHashMap<String, Source>();
	private String default_pin = "";
	private GpioController gpio = null;
	private ConcurrentHashMap<String, GpioPinDigitalOutput> pins = new ConcurrentHashMap<String, GpioPinDigitalOutput>();

	public InputSourcesImpl() {
		// getSources();
		initPi4J();
		sources = PluginGateWay.getInstance().getSources();
		createPins();
		PluginGateWay.getInstance().addObserver(this);
	}

	private void createPins() {
		if(gpio ==null)
			return;
		log.debug("Creating Pins from Sources");
		for (String key : sources.keySet()) {
			Source s = sources.get(key);
			if (!pins.containsKey(s.getGPIO_PIN())) {
				Pin pin_number = createPin(s.getGPIO_PIN());
				if (pin_number != null) {
					GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(pin_number, // PIN
							// NUMBER
							s.getName(), // PIN FRIENDLY NAME (optional)
							PinState.LOW); // PIN STARTUP STATE (optional)
					pins.put(s.getGPIO_PIN(), pin);
				}
			}
		}
	}

	private void initPi4J() {
		try {
			String pin = "6";
			if (pin.equalsIgnoreCase("6")) {
				Pin mPin = RaspiPin.GPIO_06;
			}
			try {
				gpio = OSManager.getInstance().getGpio();
				if (null == gpio)
					throw new IllegalArgumentException("GPIO Not Initialized");
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
				try {
					log.debug("Changed Source ClearPins");
					clearLEDS();
					Source source = sources.get(name);
					log.debug("Source PIN: " + source.getGPIO_PIN());
					if (pins.containsKey(source.getGPIO_PIN())) {
						log.debug("Changed Source Take Pin: " + source.getGPIO_PIN() + " High");
						GpioPinDigitalOutput pin = pins.get(source.getGPIO_PIN());
						pin.high();
					}

				} catch (Exception e) {
					log.error("Error Setting Pins: " + e);
				}
			} else {
				log.debug("Not A CustomSource Change Input to PIN: " + default_pin);
			}
		}

	}

	/**
	 * Get the RaspiPin from a String
	 * 
	 * @param number
	 * @return
	 */
	private Pin createPin(String number) {
		if (number.equalsIgnoreCase("0"))
			return RaspiPin.GPIO_00;
		if (number.equalsIgnoreCase("1"))
			return RaspiPin.GPIO_01;
		if (number.equalsIgnoreCase("2"))
			return RaspiPin.GPIO_02;
		if (number.equalsIgnoreCase("3"))
			return RaspiPin.GPIO_03;
		if (number.equalsIgnoreCase("4"))
			return RaspiPin.GPIO_04;
		if (number.equalsIgnoreCase("5"))
			return RaspiPin.GPIO_05;
		if (number.equalsIgnoreCase("6"))
			return RaspiPin.GPIO_06;
		if (number.equalsIgnoreCase("7"))
			return RaspiPin.GPIO_07;
		if (number.equalsIgnoreCase("8"))
			return RaspiPin.GPIO_08;
		if (number.equalsIgnoreCase("9"))
			return RaspiPin.GPIO_09;
		if (number.equalsIgnoreCase("10"))
			return RaspiPin.GPIO_10;
		if (number.equalsIgnoreCase("11"))
			return RaspiPin.GPIO_11;
		if (number.equalsIgnoreCase("12"))
			return RaspiPin.GPIO_12;
		if (number.equalsIgnoreCase("13"))
			return RaspiPin.GPIO_13;
		if (number.equalsIgnoreCase("14"))
			return RaspiPin.GPIO_14;
		if (number.equalsIgnoreCase("15"))
			return RaspiPin.GPIO_15;
		if (number.equalsIgnoreCase("16"))
			return RaspiPin.GPIO_16;
		if (number.equalsIgnoreCase("17"))
			return RaspiPin.GPIO_17;
		if (number.equalsIgnoreCase("18"))
			return RaspiPin.GPIO_18;
		if (number.equalsIgnoreCase("19"))
			return RaspiPin.GPIO_19;
		if (number.equalsIgnoreCase("20"))
			return RaspiPin.GPIO_20;
		return null;
	}

	@Shutdown
	public void bye() {
		log.debug("ShutDown Called");
		clearLEDS();
	}

	private void clearLEDS() {
		for (String key : pins.keySet()) {
			log.debug("Make Pin: " + key + " Low");
			GpioPinDigitalOutput pin = pins.get(key);
			pin.low();
		}
	}

}
