package org.rpi.songcast.ohz.common;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

import java.net.Inet4Address;
import java.net.InetAddress;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class RunNettySongcast {

	private static Logger log = Logger.getLogger("RunNettySongcast");

	public static void main(String[] args) {
		ResourceLeakDetector.setLevel(Level.DISABLED);
		BasicConfigurator.configure();
		try {
			Inet4Address localAddress = (Inet4Address) InetAddress.getByName("192.168.1.90");
			String uri = "ohz://239.255.255.250:51972";
			String zoneID = "b33f69011e38827aa138adc6d00cb23e";
			//OHZConnector netty = new OHZConnector(uri, zoneID, localAddress);
			OHZConnector.getInstance().run(uri, zoneID, localAddress);
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
