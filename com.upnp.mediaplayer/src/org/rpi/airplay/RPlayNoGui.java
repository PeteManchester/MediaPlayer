package org.rpi.airplay;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;


public class RPlayNoGui {
	private static Logger log = Logger.getLogger("RPlayNoGui");

	public static void main(String[] args) {
		BasicConfigurator.configure();
		
		if(args.length == 1) {
			// Name only
			new AirPlayThread(args[0]).start();
		} else if(args.length == 2) {
			// Name and password
			new AirPlayThread(args[0], args[1]).start();
		} else {
			log.error("Java port of shairport.");
			log.error("usage : java -jar " + RPlayNoGui.class.getCanonicalName() + ".jar <AP_name> [<password>]");
			System.exit(-1);
		}	
	}
}
