package org.rpi.providers;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgVolume1;

public class Template extends DvProviderAvOpenhomeOrgVolume1 {

	private Logger log = Logger.getLogger(Template.class);


	public Template(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating Template");

		
	}



}
