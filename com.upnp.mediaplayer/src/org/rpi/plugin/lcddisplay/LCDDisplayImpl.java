package org.rpi.plugin.lcddisplay;

import java.util.EventObject;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.rpi.player.IPlayerEventClassListener;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaData;
import org.rpi.playlist.CustomTrack;
import org.rpi.playlist.PlayManager;

@PluginImplementation
public class LCDDisplayImpl implements LCDDislayInterface,
		IPlayerEventClassListener {

	private static Logger log = Logger.getLogger(LCDDisplayImpl.class);




	public LCDDisplayImpl() {
		log.debug("Init LCDDisplayImpl");
		PlayManager.getInstance().addEventListener(this);
	}



	@Override
	public void handleMyEventClassEvent(EventObject e) {
		log.debug("Event Received");
		if (e instanceof EventUpdateTrackMetaData) {
			EventUpdateTrackMetaData et = (EventUpdateTrackMetaData) e;
			log.debug("Track Changed: " + et.getArtist() + " : "
					+ et.getTitle());
		} else if (e instanceof EventTrackChanged) {
			EventTrackChanged etc = (EventTrackChanged) e;
			CustomTrack track = etc.getTrack();
			if (track != null) {
				log.debug("TrackChanged: " + track.getArtist() + " : "
						+ track.getMetadata());
			} else {
				log.debug("Track was NULL");
			}
		}
	}

}
