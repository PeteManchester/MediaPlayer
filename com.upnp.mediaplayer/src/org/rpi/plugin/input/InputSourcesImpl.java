package org.rpi.plugin.input;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Shutdown;

import org.apache.log4j.Logger;
import org.rpi.os.OSManager;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventSourceChanged;
import org.rpi.player.events.EventStandbyChanged;
import org.rpi.plugingateway.PluginGateWay;
import org.rpi.sources.Source;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

@PluginImplementation
public class InputSourcesImpl implements InputSourcesInterface, Observer {

	private Logger log = Logger.getLogger(this.getClass());
	private ConcurrentHashMap<String, Source> sources = new ConcurrentHashMap<String, Source>();
	private String default_pin = "";
	private String standby_pin = "";
	private GpioController gpio = null;
	private ConcurrentHashMap<String, GpioPinDigitalOutput> pins = new ConcurrentHashMap<String, GpioPinDigitalOutput>();
	private boolean standby_state = false;

	public InputSourcesImpl() {
		// getSources();
		initPi4J();
		sources = PluginGateWay.getInstance().getSources();
		default_pin = PluginGateWay.getInstance().getDefaultSourcePin();
		standby_pin = PluginGateWay.getInstance().getStandbyPin();
		log.debug("StandbyPIn: " + standby_pin);
		createPins();
		String name = PluginGateWay.getInstance().getSourceName();
		updateSource(name);
		standby_state = PlayManager.getInstance().isStandby();
		updateStandbyState(standby_state);
		PluginGateWay.getInstance().addObserver(this);
		PlayManager.getInstance().observeProductEvents(this);
	}

	private void createPins() {
		if (gpio == null)
			return;
		if (!default_pin.equalsIgnoreCase("")) {
			log.debug("Creating Default_Pin: " + default_pin);
			Pin pin_number = createPin(default_pin);
			if (pin_number != null) {
				GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(pin_number, // PIN NUMBER
						"default_pin", // PIN FRIENDLY NAME (optional)
						PinState.LOW); // PIN STARTUP STATE (optional)
				pins.put(default_pin, pin);
			}

		}
		else
		{
			log.debug("Default Pin was not Set");
		}
		
		if(!standby_pin.equalsIgnoreCase(""))
		{
			log.debug("Creating Standby_Pin: " + standby_pin);
			Pin pin_number = createPin(standby_pin);
			if (pin_number != null) {
				GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(pin_number, // PIN NUMBER
						"standby_pin", // PIN FRIENDLY NAME (optional)
						PinState.LOW); // PIN STARTUP STATE (optional)
				pins.put(standby_pin, pin);
			}
		}
		else
		{
			log.debug("Standby Pin was not Set");
		}
		
		log.debug("Creating Pins from Sources");
		for (String key : sources.keySet()) {
			Source s = sources.get(key);
			if (!pins.containsKey(s.getGPIO_PIN())) {
				log.debug("Creating PIN: " + s.getGPIO_PIN());
				Pin pin_number = createPin(s.getGPIO_PIN());
				if (pin_number != null) {
					GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(pin_number, // PIN
							// NUMBER
							s.getName(), // PIN FRIENDLY NAME (optional)
							PinState.LOW); // PIN STARTUP STATE (optional)
					log.info("Created PIN: " + s.getGPIO_PIN());
					pins.put(s.getGPIO_PIN(), pin);
				}
				else					
				{
					log.info("PIN was not valid PIN: " +s.getGPIO_PIN());
				}
			}
		}
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
	public void update(Observable o, Object event) {

		EventBase base = (EventBase) event;
		switch (base.getType()) {
		case EVENTSOURCECHANGED:
			EventSourceChanged es = (EventSourceChanged) event;
			String name = es.getName();
			String source_type = es.getSourceType();
			if (source_type.equalsIgnoreCase("ANALOG")) {
				log.debug("Analog Source Selected, attempt to Stop Playback");
				PlayManager.getInstance().stop();
			}
			log.debug("Source Changed: " + name);
			updateSource(name);
			break;
		case EVENTSTANDBYCHANGED:
			EventStandbyChanged esb = (EventStandbyChanged) event;
			log.debug("Standby: " + esb.isStandby());
			updateStandbyState(esb.isStandby());
			break;
		}
	}
	
	private void updateStandbyState(boolean standby_state)
	{ 	
		this.standby_state = standby_state;
		updateStandbyPin(standby_state);
	}

	private void updateSource(String name) {
		if(standby_state )
		{
			log.debug("In Standby, do not update Sources");
			return;
		}
		if (gpio == null)
			return;
		if (sources.containsKey(name)) {
			try {
				log.debug("Changed Source ClearPins: " + name);
				clearLEDS(false);
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
			try {
				clearLEDS(false);
				GpioPinDigitalOutput pin = pins.get(default_pin);
				pin.high();
			} catch (Exception e) {
				log.error("Error Making Default Pin High", e);
			}
		}
	}

	private void updateStandbyPin(boolean standby) {
		if (!standby_pin.equalsIgnoreCase("")) {
			log.debug("Update Standby Pin: " + standby_pin + " Standby State: " + standby);
			if (standby) {
				log.debug("Now in Standby State, take all pins low");
				clearLEDS(true);
			} else {
				try {
					log.debug("Out of Standby take Pin: " + standby_pin + " High");
					GpioPinDigitalOutput pin = pins.get(standby_pin);
					pin.high();
					String source_name = PluginGateWay.getInstance().getSourceName();
					log.debug("Out of Standby, Set Source: " + source_name);
					updateSource(source_name);
				} catch (Exception e) {
					log.error("Error Taking StandbyPin High ", e);
				}
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
		if (number.equalsIgnoreCase("21"))
			return RaspiPin.GPIO_21;
		if (number.equalsIgnoreCase("22"))
			return RaspiPin.GPIO_22;
		if (number.equalsIgnoreCase("23"))
			return RaspiPin.GPIO_23;
		if (number.equalsIgnoreCase("24"))
			return RaspiPin.GPIO_24;
		if (number.equalsIgnoreCase("25"))
			return RaspiPin.GPIO_25;
		if (number.equalsIgnoreCase("26"))
			return RaspiPin.GPIO_26;
		if (number.equalsIgnoreCase("27"))
			return RaspiPin.GPIO_27;
		if (number.equalsIgnoreCase("28"))
			return RaspiPin.GPIO_28;
		if (number.equalsIgnoreCase("29"))
			return RaspiPin.GPIO_29;
		return null;
	}

	@Shutdown
	public void bye() {
		log.debug("ShutDown Called");
		clearLEDS(true);
	}

	private void clearLEDS(boolean clearStandbyPin) {
		if (gpio == null)
			return;
		// if(!default_pin.equalsIgnoreCase(""))
		// {
		// log.debug("Make Default Pin: " + default_pin + " Low");
		// GpioPinDigitalOutput pin = pins.get(key);
		// pin.low();
		// }
		for (String key : pins.keySet()) {
			if ((key.equalsIgnoreCase(standby_pin)) && !clearStandbyPin) {
				log.debug("Don't Clear StandbyPin");
			} else {
				log.debug("Make Pin: " + key + " Low");
				try {
					GpioPinDigitalOutput pin = pins.get(key);
					pin.low();
				} catch (Exception e) {
				}
			}
		}
	}

}
