package org.rpi.songcast.ohu.receiver;

import java.net.InetAddress;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventReceiverStatusChanged;
import org.rpi.player.events.EventStopSongcast;


public class OHUReceiverController implements Observer {
	
	private static OHUReceiverController instance = null;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private OHUConnector ohuConnector = null;
	
	public static OHUReceiverController getInstance() {
		if( instance == null) {
			instance = new OHUReceiverController();
		}
		return instance;
	}
	
	private OHUReceiverController() {
		
	}
	
	public void startReceiver(String uri, String zone, InetAddress localInetAddr ) {
		if(ohuConnector !=null) {
			ohuConnector.stop();
		}
		ohuConnector = new OHUConnector(uri, zone, localInetAddr);
		try {
			ohuConnector.run();
			log.debug("Set Status set to Playing SONGCAST");
			PlayManager.getInstance().setStatus("Playing", "SONGCAST");
			log.debug("Status set to Playing SONGCAST");
		} catch (Exception e) {
			log.error("Error Starting OHUConnector",e);
		}
	}
	
	public void stop() {
		log.debug("Stopping OHU Receiver");
		if(ohuConnector !=null) {
			ohuConnector.stop();
			ohuConnector = null;
		}
	}

	@Override
	public void update(Observable arg0, Object ev) {		
		EventBase e = (EventBase) ev;
		switch (e.getType()) {
		case EVENTSTOPSONGCAST:
			stop();
			break;
		}
	}

}
