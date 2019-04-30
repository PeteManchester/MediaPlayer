package org.rpi.providers;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.providers.*;

public class PrvConfig extends DvProviderLinnCoUkConfiguration1 implements Observer, IDisposableDevice {
	
	public PrvConfig(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating CustomConfig");
		// TODO Auto-generated constructor stub
	}

	private Logger log = Logger.getLogger(PrvConfig.class);

	@Override
	public String getName() {

		return "Config";
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		log.debug("Update: ");
		
	}

}
