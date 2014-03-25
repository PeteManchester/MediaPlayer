package org.rpi.songcast.ohm;

import org.apache.log4j.Logger;
import org.rpi.songcast.core.SongcastSocket;

public class OHUThreadRequester implements Runnable {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private boolean run = true;
	
	private SongcastSocket udpSender = null;

	
	public OHUThreadRequester(SongcastSocket udpSender)
	{
		log.debug("Creating OHURequest to send keep alive messages");
		this.udpSender = udpSender;
	}

	@Override
	public void run() {
		while(run)
		{
			if(udpSender !=null)
			{

				OHMRequestListen req = new OHMRequestListen("");
				udpSender.put(req);
			}
			try {
				Thread.sleep(900);
			} catch (InterruptedException e) {
				log.error("Error",e);
			}
			
		}		
	}
	
	public void setRun(boolean run) {
		this.run = run;
	}

}
