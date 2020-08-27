package org.rpi.mplayer;

import java.io.IOException;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelBase;
import org.rpi.config.Config;
import org.rpi.player.IPlayer;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventFinishedCurrentTrack;
import org.rpi.player.events.EventStatusChanged;
import org.rpi.player.events.EventUpdateTrackInfo;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.radio.parsers.FileParser;

public class MPlayer extends Observable implements IPlayer, Observer {

	private Logger log = Logger.getLogger(MPlayer.class);
	private Process process = null;
	private OutputReader reader = null;
	private InputWriter writer = null;
	private PositionThread position = null;

	private boolean bPaused = false;
	private boolean bPlaying = false;

	// private boolean bPreLoad = false;
	private boolean bLoading = false;

	private TrackInfo trackInfo = null;
	private long volume = 100;

	private String uniqueId = "";

	private ChannelBase current_track = null;
	private boolean bMute;// Used to mute when playing a track

	private boolean mute = false; // Used to keep track of mute status
	
	TrackInfo lastTrackInfo = null;

	/***
	 * Plays the Custom Track
	 * 
	 * @param track
	 * @return
	 */
	public boolean playTrack(ChannelBase track, long volume, boolean mute, boolean isStopped) {
		uniqueId = track.getUniqueId();
		this.volume = volume;
		this.bMute = mute;
		current_track = track;
		log.info("Starting to playTrack Id: " + uniqueId + " " + track.getFullDetails());
		// String url = track.getUri();
		String url = checkURL(track.getUri());
		log.debug("FileParser Returned: " + url);
		try {
			initProcess(url);
		} catch (Exception e) {
			log.error("Error playTrack: ", e);
		}
		return true;
	}

	/***
	 * PreLoad the Track ready for when the current Track ends
	 */
	public void preLoadTrack(ChannelBase track) {
	}

	/***
	 * Used to start a pre loaded track
	 */
	public void startTrack() {
		startPlaying();
	}

	/***
	 * If the Player is Playing we can change tracks.
	 * 
	 * @param t
	 */
	public void openFile(ChannelBase t) {
	}

	public synchronized void startPlaying() {
		log.debug("Starting to Play: " + uniqueId);
		setVolume(volume);
		if (bMute) {
			setMute(bMute);
		}
		log.debug("Started to Play: " + uniqueId);
		setStatus("Playing");
		setPlaying(true);
		bLoading = false;
		bPaused = false;
		log.debug("PositionThreadState: " + position.getState());
		if (position.getState() == State.NEW) {
			log.debug("Position Thread is in State NEW Start Thread");
			position.start();
		}
	}

	public synchronized void loaded() {
	}

	public synchronized void playingTrack() {
		startPlaying();
	}

	/***
	 * Build the string to start the process
	 * 
	 * @param url
	 * @throws IOException
	 */
	private void initProcess(String url) throws IOException {
		try {
			List<String> params = new ArrayList<String>();
			params.add(Config.getInstance().getMPlayerPath());
			params.add("-slave");
			params.add("-quiet");
			int cache_size = Config.getInstance().getMplayerCacheSize();
			if (cache_size > 0) {
				params.add("-cache");
				params.add("" + cache_size);
			}
			int cache_min = Config.getInstance().getMPlayerCacheMin();
			if (cache_min > 0) {
				params.add("-cache-min");
				params.add("" + cache_min);
			}

			trackInfo = new TrackInfo();
			trackInfo.addObserver(this);
			if (isPlayList(url)) {
				params.add("-playlist");
			}
			params.add(url);
			ProcessBuilder builder = new ProcessBuilder(params);
			builder.redirectErrorStream(true);
			process = builder.start();
			log.debug("Create new InputWriter");
			writer = new InputWriter(process);
			log.debug("Create new OutputReader");
			reader = new OutputReader(this);
			log.debug("Create new Position Thread");
			position = new PositionThread(this);
			log.debug("Create new Position Thread");
			position.setNewTrack(true);
			reader.start();
		} catch (Exception e) {
			log.error("Error initProcess: ", e);
		}
	}

	/***
	 * Is this one of the playlists we have configured
	 * 
	 * @param url
	 * @return
	 */
	private boolean isPlayList(String url) {
		List<String> pl = Config.getInstance().getMplayerPlayListDefinitions();
		for (String s : pl) {
			if (url.toLowerCase().contains(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void pause(boolean bPause) {
		bPaused = true;
		log.debug("Sending: pause");
		sendCommand("pause");
		EventStatusChanged ev = new EventStatusChanged();
		ev.setStatus("Paused");
		fireEvent(ev);
	}

	@Override
	public synchronized void stop() {
		log.debug("Sending: quit");
		sendCommand("quit");
		EventStatusChanged ev = new EventStatusChanged();
		ev.setStatus("Stopped");
		fireEvent(ev);
	}

	@Override
	public synchronized void destroy() {
		log.debug("Attempting to Stop MPlayer");
		sendCommand("quit");
		reader = null;
		writer = null;
		if (position != null) {
			position.interrupt();
			position = null;
		}
	}

	public synchronized InputWriter getCommandWriter() {
		return writer;
	}

	/**
	 * @return the bPaused
	 */
	public boolean isbPaused() {
		return bPaused;
	}

	/**
	 * @return the bPlaying
	 */
	public boolean isPlaying() {
		if (process != null) {
			if (position != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param bPlaying
	 *            the bPlaying to set
	 */
	public void setPlaying(boolean bPlaying) {
		log.debug("setPlaying: " + bPlaying);
		this.bPlaying = bPlaying;
	}

	/***
	 * Track has stopped Playing, get Next Track..
	 */
	public synchronized void stoppedPlaying() {
		writer.setStopSendingCommands(true);
		log.debug("Stopped Playing get next track: ");
		setPlaying(false);
		setStatus("Stopped");
		position.interrupt();
		position = null;
		reader = null;
		EventFinishedCurrentTrack ev = new EventFinishedCurrentTrack();
		fireEvent(ev);
	}

	/***
	 * Update OpenHome with the new Status
	 * 
	 * @param status
	 */
	public synchronized void setStatus(String status) {
		EventStatusChanged ev = new EventStatusChanged();
		ev.setStatus(status);
		ev.setTrack(current_track);
		fireEvent(ev);
	}

	public synchronized Process getProcess() {
		return this.process;
	}

	/**
	 * @return the trackInfo
	 */
	public synchronized TrackInfo getTrackInfo() {
		return trackInfo;
	}

	@Override
	public void setMute(boolean mute) {
		this.mute = mute;
		if (mute) {
			sendCommand("pausing_keep mute");
			// Bug Fix for MPlayer on Raspberry
			// sendCommand("pausing_keep volume 0 1");
		} else {
			sendCommand("pausing_keep mute");
			// Bug fix for MPlayer on Raspberry
			sendCommand("pausing_keep volume " + (volume - 1) + " 1");
			setVolume(volume);
		}
	}

	@Override
	public void setVolume(long volume) {
		this.volume = volume;
		if (!mute)
			sendCommand("pausing_keep volume " + volume + " 1");
	}

	@Override
	public void seekAbsolute(long seconds) {
		sendCommand("seek " + seconds + " 2");
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

	/***
	 * 
	 */
	public void endPositionThread() {
		if (position != null) {
			try {
				position.interrupt();
			} catch (Exception e) {
				log.error("Error Stopping Position Thread", e);
			}
		}
	}

	/***
	 * 
	 */
	@Override
	public synchronized void resume() {
		bPaused = false;
		sendCommand("pause");
		EventStatusChanged ev = new EventStatusChanged();
		ev.setStatus("Playing");
		fireEvent(ev);
	}

	/***
	 * Send Command to MPlayer
	 * 
	 * @param command
	 */
	public synchronized void sendCommand(String command) {
		if (writer != null) {
			log.debug("Sending: " + command + " TrackId: " + uniqueId);
			writer.sendCommand(command);
			log.debug("Sent: " + command + " TrackId: " + uniqueId);
		} else {
			log.info("Could Not Send Command, Writer was null: " + command);
		}
	}

	public synchronized void fireEvent(EventBase ev) {
		setChanged();
		notifyObservers(ev);
	}

	public boolean isLoading() {
		return bLoading;
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("UniqueId: " + uniqueId);
		sb.append("Writer: " + writer.toString());
		sb.append("Reader: " + reader.toString());
		return sb.toString();
	}

	@Override
	public void update(Observable o, Object evt) {
		EventBase e = (EventBase) evt;
		fireEvent(e);
	}

	private String checkURL(String url) {
		FileParser fp = new FileParser();
		return fp.getURL(url);
	}

	
	/***
	 * Check if the TrackInfo is set, if so check to see if an Update should be
	 * sent.
	 * 
	 * @return
	 */
	public boolean trackInfoSet() {
		if (trackInfo.isSet()) {
			trackInfo.setUpdated(true);
			if (!trackInfo.equals(lastTrackInfo)) {
				trackInfo.sendEvent();
				lastTrackInfo = trackInfo.cloneToValueObject();
				EventUpdateTrackInfo ev = new EventUpdateTrackInfo();
				ev.setTrackInfo(trackInfo);
				fireEvent(ev);
			} 
		}
		return trackInfo.isSet();
	}

}
