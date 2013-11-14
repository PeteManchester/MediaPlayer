package org.rpi.os;


import org.apache.log4j.Logger;


import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

/**
 * Class to manage the Pi4J initialization.
 * @author phoyle
 *
 */
public class Pi4JManager {

	private static Pi4JManager instance = null;
	private static Logger log = Logger.getLogger(Pi4JManager.class);
	private GpioController gpio = null;

	public static Pi4JManager getInstance() {
		if (instance == null) {
			instance = new Pi4JManager();
		}
		return instance;
	}

	protected Pi4JManager() {
		if (OSManager.getInstance().isRaspi()) {
			initPi4J();
		} else {
			log.warn("This is not a Raspi, do Not attempt initialize Pi4J");
		}
	}

	/**
	 * 
	 */
	private void initPi4J() {
		try {
			log.debug("Initialize Pi4J");
			setGpio(GpioFactory.getInstance());
			log.debug("Initialized Pi4J");
		} catch (Exception e) {
			log.error("Error Initializing Pi4J", e);
		}
	}

	/**
	 * 
	 */
	public void dispose() {
		if (gpio != null) {
			try {
				gpio.shutdown();
			} catch (Exception e) {
				log.error("Error Closing Pi4J",e);
			}
		}
	}

	public GpioController getGpio() {
		return gpio;
	}

	private void setGpio(GpioController gpio) {
		this.gpio = gpio;
	}

}
