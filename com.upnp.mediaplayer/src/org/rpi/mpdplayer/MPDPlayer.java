package org.rpi.mpdplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.player.IPlayer;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventCurrentTrackFinishing;
import org.rpi.player.events.EventPlayListPlayingTrackID;
import org.rpi.player.events.EventStatusChanged;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.player.events.EventVolumeChanged;
import org.rpi.playlist.CustomTrack;

public class MPDPlayer extends Observable implements IPlayer, Observer {

	private static Logger log = Logger.getLogger(MPDPlayer.class);
	HashMap<String, CustomTrack> tracks = new HashMap<String, CustomTrack>();

	private TCPConnector tcp = null;
	private CustomTrack current_track = null;
	private String current_status = "";
	private long current_volume = 100;
	private long mute_volume = 100;

	public MPDPlayer() {
		tcp = new TCPConnector(this);
		tcp.addObserver(this);
		List<String> param = new ArrayList<>();
		param.add("1");
		tcp.sendCommand(tcp.createCommand("consume", param));
	}

	@Override
	public void preLoadTrack(CustomTrack track) {
		List<String> params = new ArrayList<String>();
		params.add(track.getUri());
		params.add("1");
		HashMap<String, String> res = tcp.sendCommand(tcp.createCommand("addid", params));
		if (res.containsKey("Id")) {
			tracks.put(res.get("Id"), track);
		}
	}

	@Override
	public void loaded() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean playTrack(CustomTrack track, long volume, boolean mute) {
		current_track = track;
		// tcp.sendCommand("clear");
		// tcp.sendCommand("addid \"" + track.getUri() + "\" \"0\"");
		// tcp.sendCommand("play 0");
		List<String> commands = new ArrayList<String>();
		commands.add(tcp.createCommand("clear"));
		List<String> params = new ArrayList<String>();
		params.add(track.getUri());
		params.add("0");
		commands.add(tcp.createCommand("addid", params));
		params.clear();
		params.add("0");
		commands.add(tcp.createCommand("play", params));
		HashMap<String, String> res = tcp.sendCommand(tcp.createCommandList(commands));
		if (res.containsKey("Id")) {
			tracks.put(res.get("Id"), track);
		}
		log.debug("ADD TO PLAYLIST" + res.toString());
		return true;
	}

	@Override
	public void openFile(CustomTrack track) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause(boolean bPause) {
		List<String> params = new ArrayList<>();
		if (bPause) {
			params.add("1");
		} else {
			params.add("0");
		}
		tcp.sendCommand(tcp.createCommand("pause", params));
	}

	@Override
	public void resume() {
		pause(false);
	}

	@Override
	public void stop() {
		tcp.sendCommand(tcp.createCommand("stop"));

	}

	@Override
	public void destroy() {
		tcp.destroy();

	}

	@Override
	public void setMute(boolean mute) {
		if(mute)
		{
			mute_volume = current_volume;
			setVolume(0);
		}
		else
		{
			setVolume(mute_volume);
		}

	}

	@Override
	public void setVolume(long volume) {
		List<String> params = new ArrayList<String>();
		params.add(""+volume);
		tcp.sendCommand(tcp.createCommand("setvol", params));
	}

	@Override
	public void seekAbsolute(long seconds) {
		List<String> params = new ArrayList<>();
		params.add("0");
		params.add("" + seconds);
		tcp.sendCommand(tcp.createCommand("seek", params));
	}

	@Override
	public void startTrack() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPlaying() {
		if (current_status.equalsIgnoreCase("PLAYING") || current_status.equalsIgnoreCase("PAUSED")) {
			return true;
		}
		return false;
	}

	@Override
	public String getUniqueId() {
		// TODO Auto-generated method stub
		return null;
	}

	public synchronized void setStatus(String value) {
		current_status = value;
		EventStatusChanged ev = new EventStatusChanged();
		ev.setStatus(value);
		ev.setTrack(current_track);
		fireEvent(ev);
	}
	
	/***
	 * Used by the ICY info to update the track being played on the Radio
	 * 
	 * @param artist
	 * @param title
	 */
	public synchronized void updateInfo(String artist, String title) {
		EventUpdateTrackMetaText ev = new EventUpdateTrackMetaText();
		ev.setArtist(artist);
		ev.setTitle(title);
		//ev.setCurrentTrackId(current_track.getId());
		fireEvent(ev);
	}

	public synchronized void fireEvent(EventBase ev) {
		if(ev instanceof EventVolumeChanged)
		{
			EventVolumeChanged e = (EventVolumeChanged)ev;
			current_volume = e.getVolume();
		}
		setChanged();
		notifyObservers(ev);
	}

	@Override
	public void update(Observable arg0, Object obj) {
		EventBase e = (EventBase) obj;
		switch (e.getType()) {
		case EVENTTRACKCHANGED:
			EventTrackChanged ev = (EventTrackChanged) e;
			if (tracks.containsKey(ev.getMPD_id())) {
				CustomTrack t = tracks.get(ev.getMPD_id());
				ev.setTrack(t);
				fireEvent(ev);
				EventPlayListPlayingTrackID ep = new EventPlayListPlayingTrackID();
				ep.setId(t.getId());
				fireEvent(ep);
			}
			break;
		case EVENTCURRENTTRACKFINISHING:
			fireEvent(e);
			break;
		}

	}

}
