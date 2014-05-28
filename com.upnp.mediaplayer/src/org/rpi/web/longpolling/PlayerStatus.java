package org.rpi.web.longpolling;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelBase;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;

public class PlayerStatus implements Observer {

	private Logger log = Logger.getLogger(this.getClass());

	private static PlayerStatus instance = null;

	private String album_title = "";
	private String artist = "";
	private String album_artist = "";
	private String title = "";
	private String image_uri = "";
	private long track_duration = 0;
	private String genre = "";
	private long time_played = 0;

	private String q = "\"";
	private String colon = ":";
	private String space = " ";
	private String comma = ",";

	private String performer = "";

	public static PlayerStatus getInstance() {
		if (instance == null) {
			instance = new PlayerStatus();
		}
		return instance;
	}

	private PlayerStatus() {
		PlayManager.getInstance().observeInfoEvents(this);
		PlayManager.getInstance().observeProductEvents(this);
		PlayManager.getInstance().observeTimeEvents(this);
	}

	@Override
	public void update(Observable o, Object evt) {
		EventBase base = (EventBase) evt;
		switch (base.getType()) {
		case EVENTTRACKCHANGED:
			EventTrackChanged etc = (EventTrackChanged) evt;
			ChannelBase track = etc.getTrack();
			if (track != null) {
				album_title = track.getAlbum();
				artist = track.getArtist();
				performer  = track.getPerformer();
				album_artist = track.getAlbumArtist();
				title = track.getTitle();
				image_uri = track.getAlbumArtUri();
				track_duration = track.getDuration();
				if(track_duration > 0)
				{
					track_duration = track_duration/1000;
				}
				genre = track.getGenre();
			} else {

			}

			break;
		case EVENTUPDATETRACKMETATEXT:
			EventUpdateTrackMetaText etm = (EventUpdateTrackMetaText) evt;
			title = etm.getTitle();
			album_artist = etm.getArtist();
			break;
		case EVENTTIMEUPDATED:
			EventTimeUpdate ed = (EventTimeUpdate) evt;
			time_played = ed.getTime();
			break;
		}
		
	}

	public synchronized String getJSON() {
		
		if(performer.equalsIgnoreCase(""))
		{
			performer = album_artist;
		}
		StringBuilder sb = new StringBuilder();
		try {
			sb.append("{");
			sb.append(q + "album_title" + q + colon + space + q + cleanString(album_title) + q);
			sb.append(comma + q + "artist" + q + colon + space + q + cleanString(artist) + q);
			sb.append(comma + q + "album_artist" + q + colon + space + q + cleanString(performer) + q);
			sb.append(comma + q + "title" + q + colon + space + q + cleanString(title) + q);
			sb.append(comma + q + "image_uri" + q + colon + space + q + cleanString(image_uri) + q);
			sb.append(comma + q + "track_duration" + q + colon + space + q + track_duration + q);
			sb.append(comma + q + "genre" + q + colon + space + q + cleanString(genre) + q);
			sb.append(comma + q + "time_played" + q + colon + space + q + time_played + q);
			sb.append("}");
			// log.debug(sb.toString());
		} catch (Exception e) {
			log.error("Error getJSON", e);
		}
		return sb.toString();
	}
	
	private String cleanString(String value)
	{
		return value.replace("\"","'");
	}

}
