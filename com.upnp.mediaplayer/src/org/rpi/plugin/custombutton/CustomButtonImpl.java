package org.rpi.plugin.custombutton;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

@PluginImplementation
public class CustomButtonImpl implements CustomButtonInterface {
	
	private static Logger log = Logger.getLogger(CustomButtonImpl.class);
	
	private GpioController gpio = null;

	// provision gpio pin #02 as an input pin with its internal pull down resistor enabled
	private GpioPinDigitalInput myButton = null;
	
	public CustomButtonImpl()
	{
		log.info("CustomButtonImpl initialization");
		initPi4J();
	}
	
	private void initPi4J() {
		try {
			
			gpio = GpioFactory.getInstance();
			myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);
			addButtons();
		} catch (Exception e) {
			log.error("Error Initializing Pi4J" + e.getMessage());
		}

	}

	public void addButtons() {
		try {
			// create and register gpio pin listener
			myButton.addListener(new GpioPinListenerDigital() {
				@Override
				public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
					// display pin state on console
					log.info(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
				}
			});
		} catch (Exception e) {

		}
	}

}
