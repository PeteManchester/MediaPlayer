package org.rpi.main;

import org.apache.log4j.Logger;
import org.openhome.net.core.IMessageListener;

public class OpenHomeLogger implements IMessageListener {
	
	private static Logger log = Logger.getLogger(OpenHomeLogger.class);
	
	

	@Override
	public void message(String message) {
		log.info(message);	
	}

}
