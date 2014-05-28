package org.rpi.songcast.ohz;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

import java.net.InetAddress;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class RunNettySongcast {

	private static Logger log = Logger.getLogger("RunNettySongcast");

	public static void main(String[] args) {
		ResourceLeakDetector.setLevel(Level.DISABLED);
		BasicConfigurator.configure();
		try {
			InetAddress localAddress = InetAddress.getByName("192.168.1.72");
			String uri = "ohz://239.255.255.250:51972";
			String zoneID = "b33f69011e38827aa138adc6d00cb23e";
			OHZConnector netty = new OHZConnector(uri, zoneID, localAddress);
			netty.run();
		} catch (Exception e) {

		}
		boolean run = true;
		while (run) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				log.error(e);
			}
		}

	}

}
