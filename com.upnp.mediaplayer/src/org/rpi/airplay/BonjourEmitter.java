package org.rpi.airplay;

import java.util.*;

import javax.jmdns.*;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Emetteur Bonjour pour qu'iTunes detecte la borne airport
 * 
 * @author bencall
 * 
 */

//
public class BonjourEmitter {

	private Logger log = Logger.getLogger(this.getClass());
	JmDNS jmdns;
	private String myName = "";

	public BonjourEmitter(String name, String identifier, int port, boolean pass, InetAddress address) throws IOException {
		myName = identifier + "@" + name + "@" + address;
		log.debug("Starting Bonjour Service: " + myName);
		// Set up TXT Record
		Map<String, Object> txtRec = new HashMap<String, Object>();
		txtRec.put("txtvers", "1");
		txtRec.put("pw", String.valueOf(pass));
		txtRec.put("sr", "44100");
		txtRec.put("ss", "16");
		txtRec.put("ch", "2");
		txtRec.put("tp", "UDP");
		txtRec.put("sm", "false");
		txtRec.put("sv", "false");
		txtRec.put("ek", "1");
		txtRec.put("et", "0,1");
		txtRec.put("cn", "0,1");
		txtRec.put("vn", "3");

		// Il faut un serial bidon pour se connecter
		if (identifier == null) {
			identifier = "";
			for (int i = 0; i < 6; i++)
				identifier = identifier + Integer.toHexString((int) (Math.random() * 255)).toUpperCase();
		}

		// identifier = "b8:27:eb:4c:65:cd";

		// Zeroconf registration
		jmdns = JmDNS.create(address, name + "-jmdns");
		ServiceInfo serviceInfo = ServiceInfo.create("_raop._tcp.local.", identifier + "@" + name, port, 0, 0, txtRec);
		jmdns.registerService(serviceInfo);
		log.info("Registered for Service: \r\n" + serviceInfo.toString());
	}

	/**
	 * Stop service publishing
	 */
	public void stop() throws IOException {
		log.debug("Stop BonjourEmitter: " + myName);
		try {
			jmdns.unregisterAllServices();
		} catch (Exception e) {
			log.error("Error Unregistering Bonjour " + myName,e);
		} finally {
			try {
				jmdns.close();
			} catch (Exception ignore) {

			}
		}
	}
}
