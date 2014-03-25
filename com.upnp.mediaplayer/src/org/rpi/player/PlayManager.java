package org.rpi.player;

import java.util.Collections;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelBase;
import org.rpi.channel.ChannelPlayList;
import org.rpi.channel.ChannelRadio;
import org.rpi.channel.ChannelSongcast;
import org.rpi.config.Config;
import org.rpi.mpdplayer.MPDPlayerController;
import org.rpi.mplayer.MPlayerController;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventFinishedCurrentTrack;
import org.rpi.player.events.EventMuteChanged;
import org.rpi.player.events.EventPlayListPlayingTrackID;
import org.rpi.player.events.EventPlayListStatusChanged;
import org.rpi.player.events.EventPlayListUpdateShuffle;
import org.rpi.player.events.EventRadioPlayName;
import org.rpi.player.events.EventRadioPlayingTrackID;
import org.rpi.player.events.EventRadioStatusChanged;
import org.rpi.player.events.EventRequestVolumeDec;
import org.rpi.player.events.EventRequestVolumeInc;
import org.rpi.player.events.EventStandbyChanged;
import org.rpi.player.events.EventStatusChanged;
import org.rpi.player.events.EventStopSongcast;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.player.events.EventVolumeChanged;
import org.rpi.player.observers.ObservableAVTransport;
import org.rpi.player.observers.ObservableInfo;
import org.rpi.player.observers.ObservablePlayList;
import org.rpi.player.observers.ObservableProduct;
import org.rpi.player.observers.ObservableRadio;
import org.rpi.player.observers.ObservableVolume;
import org.rpi.player.observers.ObservsableTime;

public class PlayManager implements Observer {

	private ChannelBase current_track = null;
	private CopyOnWriteArrayList<ChannelPlayList> tracks = new CopyOnWriteArrayList<ChannelPlayList>();
	private CopyOnWriteArrayList<String> shuffled_tracks = new CopyOnWriteArrayList<String>();

	private static Logger log = Logger.getLogger(PlayManager.class);
	private IPlayerController mPlayer = null;

	private boolean repeatPlayList = false;
	private boolean shuffle = false;
	private boolean bPaused;
	private boolean standby = true;

	// For Volume
	private long volume = 100;
	private long mplayer_volume = 100;
	private boolean bMute;
	private boolean bExternalVolume = false;

	// Observable Classes
	private ObservsableTime obsvTime = new ObservsableTime();
	private ObservableInfo obsvInfo = new ObservableInfo();
	private ObservableVolume obsvVolume = new ObservableVolume();
	private ObservableRadio obsvRadio = new ObservableRadio();
	private ObservablePlayList obsvPlayList = new ObservablePlayList();
	private ObservableProduct obsvProduct = new ObservableProduct();
	private ObservableAVTransport obsvAVTransport = new ObservableAVTransport();
	private ObservableSongcast obsvSongcast = new ObservableSongcast();
	private String status = "";

	private static PlayManager instance = null;

	/**
	 * SingleInstance of the PlayManager
	 * 
	 * @return
	 */
	public static PlayManager getInstance() {
		if (instance == null) {
			instance = new PlayManager();
		}
		return instance;
	}

	/**
	 * 
	 */
	private PlayManager() {
		if (Config.player.equalsIgnoreCase("MPD")) {
			log.debug("MPD Player Selected");
			mPlayer = new MPDPlayerController();
		} else {
			log.debug("MPlayer Selected");
			mPlayer = new MPlayerController();
		}
		mPlayer.addObserver(this);
	}

	/**
	 * Play This Track If MPlayer is already running destroy it
	 * 
	 * @param t
	 */
	private void playThis(ChannelBase t) {
		if (t != null) {
			EventStopSongcast ev = new EventStopSongcast();
			obsvSongcast.notifyChange(ev);
			current_track = t;
			long v = mplayer_volume;
			if (!isUseExternalVolume())
				v = volume;
			mPlayer.playThis(t, v, bMute);
		}
	}

	/**
	 * Get the Next Track when not in shuffle Mode
	 * 
	 * @param offset
	 * @return
	 */
	public ChannelPlayList getNextTrack(int offset) {
		if (shuffle) {
			return getRandomTrack(offset);
		} else {
			return getTrack(offset);
		}
	}

	/**
	 * Get the Next Random Track
	 * 
	 * @param offset
	 * @return
	 */
	private ChannelPlayList getRandomTrack(int offset) {
		if (current_track != null) {
			if ((current_track instanceof ChannelPlayList)) {
				try {
					int i = 0;
					for (String t : getShuffledTracks()) {
						if (t.equalsIgnoreCase("" + current_track.getId())) {
							break;
						}
						i++;
					}
					if (getShuffledTracks().size() > i + offset) {
						if (i + offset >= 0) {
							String track_id = getShuffledTracks().get(i + offset);
							ChannelPlayList newTrack = getTrackFromId(Integer.parseInt(track_id));
							log.debug("Returning Next Shuffled Track: " + newTrack.getUri());
							return (newTrack);
						}
					} else {
						log.info("We have Reached the End of the PlayList");
						if (isRepeatPlayList()) {
							log.info("Repeat Playlsit is Set so start again...");
							shuffleTracks();
							if (getShuffledTracks().size() > 0) {
								String track_id = getShuffledTracks().get(0);
								ChannelPlayList newTrack = getTrackFromId(Integer.parseInt(track_id));
								return newTrack;
							}
						} else {
							return null;
						}
					}
				} catch (Exception e) {
					log.error("Error GetNextTrack: ", e);
				}
			} else {

			}
		}
		if (shuffled_tracks.size() > 0) {
			String id = shuffled_tracks.get(0);
			ChannelPlayList t = getTrackFromId(Integer.parseInt(id));
			return t;
		}
		return null;
	}

	/***
	 * Get next or previous track
	 * 
	 * @param offset
	 * @return
	 */
	private ChannelPlayList getTrack(int offset) {
		if (current_track != null)
			try {
				log.debug("Getting Next Track, CurrentTrack is: " + current_track.getUri());
				int i = 0;
				for (ChannelPlayList t : getTracks()) {
					if (current_track.getId() == t.getId()) {
						break;
					}
					i++;
				}
				if (getTracks().size() > i + offset) {
					if (i + offset >= 0) {
						ChannelPlayList newTrack = getTracks().get(i + offset);
						log.debug("Returning Next Track: " + newTrack.getUri());
						return (newTrack);
					}
				} else {
					log.info("We have Reached the End of the PlayList");
					if (isRepeatPlayList()) {
						log.info("Repeat Playlsit is Set so start again...");
						if (getTracks().size() > 0) {
							ChannelPlayList newTrack = getTracks().get(0);
							return newTrack;
						}
					}
				}
			} catch (Exception e) {
				log.error("Error GetNextTrack: ", e);
			}
		else {
			if (tracks.size() > 0) {
				return tracks.get(0);
			}
		}
		return null;
	}

	/***
	 * Get the Custom Track from the List
	 * 
	 * @param index
	 * @return
	 */
	public ChannelPlayList getTrackFromIndex(int index) {
		log.debug("GetTrackFromIndex: " + index);
		try {
			return tracks.get(index);
		} catch (Exception e) {
			log.error("Error gettingTrackFromId: ", e);
		}
		return null;
	}

	/***
	 * Return a Custom Track from the given Track Id.
	 * 
	 * @param id
	 * @return
	 */
	public ChannelPlayList getTrackFromId(int id) {
		log.debug("GetTrakcFromId: " + id);
		for (ChannelPlayList t : tracks) {
			if (t.getId() == id) {
				return t;
			}
		}
		return null;
	}

	public synchronized boolean isStandby() {
		return standby;
	}

	public synchronized void setStandby(boolean standby) {
		this.standby = standby;
		EventStandbyChanged ev = new EventStandbyChanged();
		ev.setStandby(standby);
		obsvProduct.notifyChange(ev);
		if (standby)
			stop();
	}

	/**
	 * 
	 * @return
	 */
	public boolean isRepeatPlayList() {
		return repeatPlayList;
	}

	/**
	 * 
	 * @param repeatPlayList
	 */
	public void setRepeatPlayList(boolean repeatPlayList) {
		this.repeatPlayList = repeatPlayList;
	}

	/**
	 * @return the shuffle
	 */
	public boolean isShuffle() {
		return shuffle;
	}

	/**
	 * @param shuffle
	 *            the shuffle to set
	 */
	public void setShuffle(boolean shuffle) {
		if (shuffle)
			shuffleTracks();
		this.shuffle = shuffle;
	}

	public void updateShuffle(boolean shuffle) {
		setShuffle(shuffle);
		EventPlayListUpdateShuffle ev = new EventPlayListUpdateShuffle();
		ev.setShuffle(shuffle);
		obsvPlayList.notifyChange(ev);
	}

	/**
	 * Add the Next Shuffle Track Find the current track index in the shuffle
	 * list and insert after
	 * 
	 * @param t
	 */
	private void addAsNextShuffleTrack(ChannelPlayList t) {
		int this_track = getTrackIndexShuffled(t.getId());
		if (this_track >= 0) {
			shuffled_tracks.remove(this_track);
		}
		if (current_track != null) {
			if ((current_track instanceof ChannelPlayList)) {
				int index = getTrackIndexShuffled(current_track.getId());
				if (index >= 0) {
					shuffled_tracks.add(index + 1, "" + t.getId());
					return;
				}
			}
		}
		shuffled_tracks.add(0, "" + t.getId());

	}

	/***
	 * Shuffle the tracks
	 */
	private void shuffleTracks() {
		shuffled_tracks.clear();
		for (ChannelPlayList t : tracks) {
			shuffled_tracks.add("" + t.getId());
		}
		long seed = System.nanoTime();
		Collections.shuffle(shuffled_tracks, new Random(seed));
		if (current_track != null) {
			if (current_track instanceof ChannelPlayList) {
				log.debug("We have shuffled so set the current track to index ZERO");
				int index = getTrackIndexShuffled(current_track.getId());
				shuffled_tracks.remove(index);
				shuffled_tracks.add(0, "" + current_track.getId());
			}
		}
	}

	/**
	 * Generate a Random number in the range
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	private int generateRandonNumber(int min, int max) {
		long seed = System.nanoTime();
		Random rand = new Random(seed);
		int limit = (max - min) + min;
		if (limit == 0)
			limit = 1;
		int res = rand.nextInt(limit);
		log.debug("Random Number in range " + min + " - " + max + " = " + res);
		return res;
	}

	/**
	 * @return the tracks
	 */
	private synchronized CopyOnWriteArrayList<ChannelPlayList> getTracks() {
		return tracks;
	}

	/**
	 * @return the shuffled tracks
	 */
	private synchronized CopyOnWriteArrayList<String> getShuffledTracks() {
		return shuffled_tracks;
	}

	/**
	 * Set the tracks from ProviderPlaylist Used at the start after reading
	 * playlist from the xml file.
	 * 
	 * @param tracks
	 */
	public synchronized void setTracks(CopyOnWriteArrayList<ChannelPlayList> tracks) {
		this.tracks = (CopyOnWriteArrayList<ChannelPlayList>) tracks.clone();
	}

	public synchronized void setCurrentTrack(ChannelBase track) {
		current_track = track;
		log.debug("Current Track Id: " + track.getId());
	}

	/**
	 * 
	 * @return
	 */
	public synchronized ChannelBase getCurrentTrack() {
		return current_track;
	}

	/**
	 * 
	 */
	public synchronized void deletedAllTracks() {
		tracks.clear();
		shuffled_tracks.clear();
	}

	public synchronized void deletedTrack(long iD) {
		int index = getTrackIndex(iD);
		if (index >= 0)
			tracks.remove(index);
		if (shuffled_tracks.contains("" + iD)) {
			shuffled_tracks.remove("" + iD);
		}
	}

	/**
	 * Pause track
	 * 
	 * @param bPause
	 */
	public synchronized void pause(boolean bPause) {
		if (mPlayer.isPlaying()) {
			mPlayer.pause(bPause);
			setPaused(bPause);
			setStatus("Paused");
		}
	}

	/**
	 * 
	 * @param paused
	 */
	private void setPaused(boolean paused) {
		bPaused = paused;
	}

	/**
	 * 
	 * @return
	 */
	private boolean isPaused() {
		return bPaused;
	}

	/**
	 * Stop playin Track
	 */
	public synchronized void stop() {
		if (mPlayer.isPlaying()) {
			mPlayer.stop();
		}
	}

	/**
	 * Play track with Index Determine if shuffle is enabled, if we are not
	 * playing anything shuffle the tracks and set this track as the top track,
	 * else set this track as the top track and play
	 * 
	 * @param index
	 */
	public synchronized void playIndex(long index) {
		ChannelPlayList t = getTrackFromIndex((int) index);
		if (shuffle) {
			if (!mPlayer.isPlaying()) {
				shuffleTracks();
			}
			addAsNextShuffleTrack(t);
		}
		if (t != null) {
			playThis(t);
		} else {
			log.debug("Next Track was NULL");
		}
	}

	public synchronized void playTrackId(long id) {
		ChannelPlayList t = getTrackFromId((int) id);
		if (t != null) {
			playThis(t);
		} else {
			log.debug("Next Track was NULL");
		}
	}

	/**
	 * Play Track If Paused the resume If not Paused then get Next Track and
	 * play it
	 */
	public synchronized void play() {
		if (isPaused()) {
			if (mPlayer.isPlaying()) {
				mPlayer.resume();
			}
			setStatus("Playing");
			setPaused(false);
		} else {
			// if (!(status.equalsIgnoreCase("PLAYING") ||
			// status.equalsIgnoreCase("BUFFERING"))) {
			if (current_track == null) {
				log.debug("CurrentTrack was NULL");
			}
			if (shuffle)
				shuffleTracks();
			ChannelPlayList t = getNextTrack(1);
			if (t != null) {
				playThis(t);
			}
			// }
			// else
			// {
			// log.warn("Track is Already Playing, do not Play");
			// }
		}
	}

	/**
	 * Play a Radio Channel
	 * 
	 * @param c
	 */
	public synchronized void playRadio(ChannelRadio c) {
		log.debug("Play Radio Id:  " + c.getId());
		playThis(c);
	}

	/**
	 * Play a Songcast Channel
	 * 
	 * @param track
	 */
	public void playSongcast(ChannelSongcast track) {
		log.debug("Playing Songcast Channel. Stop Playing current Track");
		setCurrentTrack(track);
		stop();
	}

	public synchronized void playAV(ChannelPlayList c) {
		log.debug("Play AV Track :  " + c.getUri());
		playThis(c);
	}

	/**
	 * Used by Alarm Plugin to Start Playing a radio channel by name
	 * 
	 * @param name
	 */
	public synchronized void playRadio(String name) {
		EventRadioPlayName ev = new EventRadioPlayName();
		ev.setName(name);
		obsvRadio.notifyChange(ev);
	}

	/***
	 * Play the Next Track
	 */
	public synchronized void nextTrack() {
		ChannelPlayList t = getNextTrack(1);
		if (t != null) {
			playThis(t);
		}
	}

	/***
	 * Play the Previous Track
	 */
	public synchronized void previousTrack() {
		if (current_track != null) {
			if (current_track.getTime() > 0 && current_track.getTime() >= 5) {
				playThis(current_track);
				return;
			}

		}
		ChannelPlayList t = getNextTrack(-1);
		if (t != null) {
			playThis(t);
		}
	}

	/**
	 * Seek time in track, does not work for certain format types and VBR MP3's
	 * due to issue with MPLayer
	 * 
	 * @param seconds
	 */
	public synchronized void seekAbsolute(long seconds) {
		if (mPlayer.isPlaying()) {
			mPlayer.seekAbsolute(seconds);
		}
	}

	// Volume Control

	/**
	 * Set the Volume to this value
	 * 
	 * @param volume
	 */
	public synchronized void setVolume(long volume) {
		if (!bMute) {
			if (this.volume < 0 || volume > 100) {
				log.debug("Volume is less than Zero, assume the DAC doesn't support Hardware Volume Control");
				return;
			}
			this.volume = volume;
			log.debug("Set Volume");
			EventVolumeChanged ev = new EventVolumeChanged();
			ev.setVolume(volume);
			obsvVolume.notifyChange(ev);
			{
				if (mPlayer.isActive()) {
					if (!isUseExternalVolume())
						mPlayer.setVolume(volume);
				}
			}
		}
	}

	/**
	 * Set Mute
	 * 
	 * @param mute
	 */
	public synchronized void setMute(boolean mute) {
		if (volume < 0) {
			log.debug("Volume is less than Zero, assume the DAC doesn't support Hardware Volume Control");
			return;
		}
		this.bMute = mute;
		EventMuteChanged em = new EventMuteChanged();
		em.setMute(mute);
		obsvProduct.notifyChange(em);
		obsvVolume.notifyChange(em);

		// if (mPlayer != null) {
		if (mPlayer.isActive()) {
			if (!isUseExternalVolume())
				mPlayer.setMute(mute);
		}
		bMute = mute;
	}

	/**
	 * Insert a track
	 * 
	 * @param aAfterId
	 * @param track
	 */
	public synchronized void insertTrack(long aAfterId, ChannelPlayList track) {
		insertAfterTrack(aAfterId, track);
		if (shuffle) {
			insertAfterShuffleTrack(aAfterId, track);
		}
	}

	/**
	 * Insert into our main list of tracks
	 * 
	 * @param aAfterId
	 * @param track
	 */
	private void insertAfterTrack(long aAfterId, ChannelPlayList track) {
		int index = 0;
		if (aAfterId != 0)
			index = getTrackIndex(aAfterId) + 1;
		tracks.add(index, track);
	}

	/**
	 * Insert into the shuffle list
	 * 
	 * @param aAfterId
	 * @param track
	 */
	private void insertAfterShuffleTrack(long aAfterId, ChannelPlayList track) {
		int index = 0;
		if (aAfterId != 0)
			index = getTrackIndexShuffled(aAfterId);
		if (current_track != null) {
			int current_index = getTrackIndexShuffled(current_track.getId());
			if (current_index == index) {
				shuffled_tracks.add(index + 1, "" + track.getId());
			} else {
				int random = generateRandonNumber(index, shuffled_tracks.size());
				shuffled_tracks.add(random, "" + track.getId());
			}
		} else {
			if (aAfterId != 0) {
				int random = generateRandonNumber(index, shuffled_tracks.size());
				shuffled_tracks.add(random, "" + track.getId());
			} else {
				shuffled_tracks.add(index, "" + track.getId());
			}
		}
	}

	/***
	 * Find the index of the Track Number in the main List
	 * 
	 * @param aAfterId
	 * @return
	 */
	private int getTrackIndex(long aAfterId) {
		int i = 0;
		for (ChannelPlayList t : getTracks()) {
			if (aAfterId == t.getId()) {
				return i;
			}
			i++;
		}
		return -99;
	}

	/***
	 * Find the index of the Track Number in the shuffled List
	 * 
	 * @param aAfterId
	 * @return
	 */
	private int getTrackIndexShuffled(long aAfterId) {
		int i = 0;
		for (String t : getShuffledTracks()) {
			if (t.equalsIgnoreCase("" + aAfterId)) {
				return i;
			}
			i++;
		}
		return -99;
	}

	/***
	 * Delete All Tracks
	 */
	public synchronized void deleteAllTracks() {
		deletedAllTracks();
		if ((getCurrentTrack() instanceof ChannelPlayList)) {
			current_track = null;
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
		}

	}

	/***
	 * Delete a Track, if it is playing stop the player
	 * 
	 * @param iD
	 */
	public synchronized void DeleteTrack(long iD) {
		deletedTrack(iD);
		ChannelBase t = getCurrentTrack();
		if (t != null)
			if (t.getId() == iD && (t instanceof ChannelPlayList)) {
				if (mPlayer.isPlaying()) {
					mPlayer.stop();
				}
			}
	}

	/***
	 * Destroy
	 */
	public synchronized void destroy() {
		log.debug("Start of destroy");
		if (mPlayer.isActive()) {
			log.debug("Attempt to Destroy MPlayer");
			mPlayer.destroy();
		}
	}

	/***
	 * Set the Status of the Track
	 * 
	 * @param status
	 */
	public synchronized void setStatus(String status) {
		log.debug("SetStatus: " + status);
		this.status = status;
		EventTrackChanged ev = new EventTrackChanged();
		if (status.equalsIgnoreCase("PLAYING")) {

			if (current_track != null) {
				playingTrack(current_track.getId());
				ev.setTrack(current_track);
				try {
					obsvInfo.notifyChange(ev);
				} catch (Exception e) {
					log.error("Error Notify: " + e);
				}
			}

		}
		if (current_track instanceof ChannelRadio) {
			EventRadioStatusChanged evr = new EventRadioStatusChanged();
			evr.setStatus(status);
			obsvRadio.notifyChange(evr);
		} else if (current_track instanceof ChannelSongcast) {
			// TODO borrowed EventPlayListChanged, may need to create one for
			// Songcast.
			EventPlayListStatusChanged evr = new EventPlayListStatusChanged();
			evr.setStatus(status);
			obsvSongcast.notifyChange(evr);
		} else if (current_track instanceof ChannelPlayList) {
			EventPlayListStatusChanged evr = new EventPlayListStatusChanged();
			evr.setStatus(status);
			obsvPlayList.notifyChange(evr);
		}
	}

	/***
	 * Update Control Point we are playing a track
	 * 
	 * @param iD
	 */
	public synchronized void playingTrack(int iD) {
		if (current_track instanceof ChannelRadio) {
			EventRadioPlayingTrackID evrp = new EventRadioPlayingTrackID();
			evrp.setId(iD);
			obsvRadio.notifyChange(evrp);
		} else {
			EventPlayListPlayingTrackID evrp = new EventPlayListPlayingTrackID();
			evrp.setId(iD);
			obsvPlayList.notifyChange(evrp);
		}
	}

	/***
	 * Increase the Volume
	 * 
	 * @return
	 */
	public synchronized long incVolume() {
		EventRequestVolumeInc ev = new EventRequestVolumeInc();
		obsvVolume.notifyChange(ev);
		if (volume < 100) {
			long v = volume;
			v++;
			setVolume(v);
		}
		return volume;
	}

	/***
	 * Decrease the Volume
	 * 
	 * @return
	 */
	public synchronized long decVolume() {
		EventRequestVolumeDec ev = new EventRequestVolumeDec();
		obsvVolume.notifyChange(ev);
		if (volume > 0) {
			long v = volume;
			v--;
			setVolume(v);
		}
		return volume;
	}

	/***
	 * Get the Volume
	 * 
	 * @return
	 */
	public synchronized long getVolume() {
		return volume;
	}

	@Override
	public void update(Observable paramObservable, Object obj) {
		EventBase e = (EventBase) obj;
		switch (e.getType()) {
		case EVENTFINISHEDCURRENTTRACK:
			try {
				EventFinishedCurrentTrack evct = (EventFinishedCurrentTrack) e;
				if (evct.isQuit()) {
					log.debug("Track was Stopped, do not select Next Track");
				} else {
					log.debug("Track Stopped, get Next Track");
					ChannelPlayList t = getNextTrack(1);
					if (t != null) {
						playThis(t);
					}
				}
			} catch (Exception etf) {
				log.error("Error EVENTFINISHEDCURRENTTRACK", etf);
			}
			break;
		case EVENTCURRENTTRACKFINISHING:
			log.debug("Current Track is going to finish, get NextTrack and PreLoad");
			ChannelPlayList t = getNextTrack(1);
			if (t != null) {
				mPlayer.preLoadTrack(t);
			}
			break;
		case EVENTPLAYLISTPLAYINGTRACKID:
			obsvPlayList.notifyChange(e);
			break;
		case EVENTTIMEUPDATED:
			try {
				obsvTime.notifyChange(e);
				if (current_track != null) {
					EventTimeUpdate ed = (EventTimeUpdate) e;
					current_track.setTime(ed.getTime());
				}
			} catch (Exception etu) {
				log.error("Error EVENTTIMEUPDATED", etu);
			}
			break;
		case EVENTSTATUSCHANGED:
			try {
				EventStatusChanged es = (EventStatusChanged) e;
				log.debug("EventStatusChanged: " + es.getStatus());
				setStatus(es.getStatus());
				obsvAVTransport.notifyChange(es);
			} catch (Exception esc) {
				log.error("Error EVENTSTATUSCHANGED", esc);
			}
			break;
		case EVENTDURATIONUPDATE:
			try {
				obsvTime.notifyChange(e);
			} catch (Exception edu) {
				log.error("Error EVENTDURATIONUPDATE", edu);
			}
			break;
		case EVENTUPDATETRACKINFO:
			try {
				obsvInfo.notifyChange(e);
			} catch (Exception eut) {
				log.error("Error EVENTUPDATETRACKINFO", eut);
			}
			break;
		case EVENTUPDATETRACKMETATEXT:
			try {
				EventUpdateTrackMetaText etm = (EventUpdateTrackMetaText) e;
				if (current_track != null) {
					if (current_track instanceof ChannelRadio) {
						if (current_track.isICYReverse()) {
							String title = etm.getArtist();
							String artist = etm.getTitle();
							etm.setTitle(title);
							etm.setArtist(artist);
						}
						String metatext = current_track.updateTrack(etm.getTitle(), etm.getArtist());
						etm.setMetaText(metatext);
						try {
							obsvInfo.notifyChange(etm);
						} catch (Exception ex) {
							log.error("Error obsvInfo.notifyChange", ex);
						}
					}
				}
			} catch (Exception etm) {
				log.error("Error EVENTUPDATETRACKMETATEXT", etm);
			}
			break;
		case EVENTLOADED:
			try {
				log.debug("Track Loaded");
			} catch (Exception etl) {
				log.error("Error EVENTLOADED", etl);
			}
			break;
		case EVENTTRACKCHANGED:
			EventTrackChanged etc = (EventTrackChanged) e;
			current_track = etc.getTrack();
			break;
		case EVENTVOLUMECHANGED:
			try {
				EventVolumeChanged ev = (EventVolumeChanged) e;
				volume = ev.getVolume();
				obsvVolume.notifyChange(e);
			} catch (Exception ex) {

			}
			break;
		// case EVENTMUTECHANGED:
		// try {
		// //EventMuteChanged emc = (EventMuteChanged) e;
		//
		// } catch (Exception exm) {
		//
		// }
		}
	}

	/**
	 * Register for Time Events
	 * 
	 * @param o
	 */
	public synchronized void observeTimeEvents(Observer o) {
		obsvTime.addObserver(o);
	}

	/**
	 * Register for Info Events
	 * 
	 * @param o
	 */
	public synchronized void observeInfoEvents(Observer o) {
		obsvInfo.addObserver(o);
	}

	/**
	 * Register for Volume Events
	 * 
	 * @param o
	 */
	public synchronized void observeVolumeEvents(Observer o) {
		obsvVolume.addObserver(o);
	}

	/**
	 * Register for PlayList Events
	 * 
	 * @param o
	 */
	public synchronized void observePlayListEvents(Observer o) {
		obsvPlayList.addObserver(o);
	}

	/**
	 * Register for Product Events
	 * 
	 * @param o
	 */
	public synchronized void observeProductEvents(Observer o) {
		obsvProduct.addObserver(o);
	}

	/**
	 * Register for Radio Events
	 * 
	 * @param o
	 */
	public synchronized void observeRadioEvents(Observer o) {
		obsvRadio.addObserver(o);
	}

	/**
	 * Register for Songcast Events
	 * 
	 * @param prvReceiver
	 */
	public void observeSongcastEvents(Observer o) {
		obsvSongcast.addObserver(o);

	}

	public void observeAVEvents(Observer o) {
		obsvAVTransport.addObserver(o);

	}

	/**
	 * Used by Songcast to update the time info
	 * 
	 * @param e
	 */
	public void updateTime(EventTimeUpdate e) {
		obsvTime.notifyChange(e);
	}

	/**
	 * Used by Songcast to update the metat text
	 * 
	 * @param e
	 */
	public void updateTrackInfo(EventBase e) {
		try {
			obsvInfo.notifyChange(e);
		} catch (Exception ex) {
			log.error("Error obsvInfo.notifyChange", ex);
		}
	}

	public synchronized boolean isUseExternalVolume() {
		return bExternalVolume;
	}

	public synchronized void setUseExternalVolume(boolean bExternalVolume) {
		this.bExternalVolume = bExternalVolume;
	}

	public synchronized void toggleMute() {
		setMute(!bMute);
	}

	public synchronized boolean getMute() {
		return bMute;
	}

	public void pause() {
		pause(!bPaused);
	}

}
