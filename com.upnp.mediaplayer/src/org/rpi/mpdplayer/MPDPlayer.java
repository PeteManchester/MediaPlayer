package org.rpi.mpdplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.player.IPlayer;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventFinishedCurrentTrack;
import org.rpi.player.events.EventMuteChanged;
import org.rpi.player.events.EventPlayListPlayingTrackID;
import org.rpi.player.events.EventStatusChanged;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.player.events.EventVolumeChanged;
import org.rpi.playlist.CustomTrack;
import org.rpi.radio.parsers.FileParser;

public class MPDPlayer extends Observable implements IPlayer, Observer {

	private static Logger log = Logger.getLogger(MPDPlayer.class);
	HashMap<String, CustomTrack> tracks = new HashMap<String, CustomTrack>();

	private TCPConnector tcp = null;
	private CustomTrack current_track = null;
	private String current_status = "";
	private long current_volume = 100;
	private long mute_volume = 100;
	private boolean bMute = false;
	private boolean bStopRequest = false;

	public MPDPlayer() {
		tcp = new TCPConnector(this);
		tcp.addObserver(this);
		List<String> param = new ArrayList<String>();
		param.add("1");
		tcp.sendCommand(tcp.createCommand("consume", param));
	}

	@Override
	public void preLoadTrack(CustomTrack track) {
		log.debug("PreLoad Next Track: " + track.getUri());
		List<String> params = new ArrayList<String>();
		String url = checkURL(track.getUri());
		params.add(url);
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
		EventStatusChanged ev = new EventStatusChanged();
		ev.setStatus("Buffering");
		fireEvent(ev);
		List<String> commands = new ArrayList<String>();
		commands.add(tcp.createCommand("clear"));
		List<String> params = new ArrayList<String>();
		String url = checkURL(track.getUri());
		params.add(url);
		params.add("0");
		commands.add(tcp.createCommand("addid", params));
		params.clear();
		params.add("0");
		commands.add(tcp.createCommand("play", params));
		HashMap<String, String> res = tcp.sendCommand(tcp.createCommandList(commands));
		tracks.clear();
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
		List<String> params = new ArrayList<String>();
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
		bStopRequest = true;
		tcp.sendCommand(tcp.createCommand("stop"));

	}

	@Override
	public void destroy() {
		tcp.destroy();

	}

	@Override
	public void setMute(boolean mute) {
		bMute = mute;
		if (mute) {
			mute_volume = current_volume;
			setVolumeInternal(0);
		} else {
			if (mute_volume == 100) {
				setVolumeInternal(99);
			}
			setVolumeInternal(mute_volume);
		}

	}

	private void setVolumeInternal(long volume) {
		List<String> params = new ArrayList<String>();
		params.add("" + volume);
		tcp.sendCommand(tcp.createCommand("setvol", params));
	}

	@Override
	public void setVolume(long volume) {
		if (!bMute) {
			setVolumeInternal(volume);
		}
	}

	@Override
	public void seekAbsolute(long seconds) {
		List<String> params = new ArrayList<String>();
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
		log.warn("DONT DO ANYTHING");
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
		fireEvent(ev);
	}

	public synchronized void fireEvent(EventBase ev) {
		if (ev instanceof EventVolumeChanged) {
			EventVolumeChanged e = (EventVolumeChanged) ev;
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
				removeTrack(ev.getMPD_id());
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
		case EVENTSTATUSCHANGED:
			EventStatusChanged es = (EventStatusChanged) e;
			log.debug("Status Changed: " + es.getStatus());
			current_status = es.getStatus();
			es.setTrack(current_track);
			if (es.getStatus().equalsIgnoreCase("STOPPED")) {
				if (!bStopRequest) {
					bStopRequest = false;
					EventFinishedCurrentTrack efc = new EventFinishedCurrentTrack();
					fireEvent(efc);
				}
			}
			else
			{
				
			}
			fireEvent(es);
			break;
		case EVENTVOLUMECHANGED:
			if (!bMute) {
				// If we are not on Mute forward the change of volume
				fireEvent(e);
			}
			break;
		default:

			fireEvent(e);
		}

	}

	private void removeTrack(String track_id) {
		try {
			if (tracks.containsKey(track_id)) {
				tracks.remove(track_id);
			}
		} catch (Exception e) {
			log.error("Error Remvoing Track: " + track_id, e);
		}
	}

	/**
	 * MPD Player will not play .pls,.m3u or .asx so we check here first
	 * 
	 * @param url
	 * @return
	 */
	private String checkURL(String url) {
		FileParser fp = new FileParser();
		return fp.getURL(url);
	}

}
