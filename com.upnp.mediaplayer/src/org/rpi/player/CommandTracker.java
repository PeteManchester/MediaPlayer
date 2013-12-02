package org.rpi.player;

import org.apache.log4j.Logger;

/**
 * Needed because some control points when playing a track will send a seekId
 * and a play request, most control points just send a seekId The theory is that
 * we measure the time between the seekId and play request, if it is very short
 * we ignore the play request, because the seekId is enough to play our track. * 
 * 
 * @author phoyle
 * 
 */

public class CommandTracker {
	
	private Logger log = Logger.getLogger(this.getClass());

	private long last_time = 0;
	private String last_command = "";

	public boolean setRequest(String s) {
		boolean res = true;
		long now = System.currentTimeMillis();
		if (last_command.toUpperCase().startsWith("SEEK")) {
			if (s.equalsIgnoreCase("PLAY")) {	
				long time_span = now - last_time;
				if (time_span < 1000) {
					log.debug("Ignore this request: " + s + " Time since Seek: " + time_span );
					res = false;
				}
			}
		}
		last_time = now;
		last_command=s;
		return res;
	}

}
