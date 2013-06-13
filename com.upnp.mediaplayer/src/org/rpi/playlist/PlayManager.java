package org.rpi.playlist;

import java.util.Collections;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.rpi.mplayer.MPlayer;
import org.rpi.player.IPlayer;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventFinishedCurrentTrack;
import org.rpi.player.events.EventMuteChanged;
import org.rpi.player.events.EventPlayListPlayingTrackID;
import org.rpi.player.events.EventPlayListStatusChanged;
import org.rpi.player.events.EventPlayListUpdateShuffle;
import org.rpi.player.events.EventRadioPlayingTrackID;
import org.rpi.player.events.EventRadioStatusChanged;
import org.rpi.player.events.EventStatusChanged;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaData;
import org.rpi.player.events.EventVolumeChanged;
import org.rpi.providers.PrvInfo;
import org.rpi.providers.PrvPlayList;
import org.rpi.providers.PrvProduct;
import org.rpi.providers.PrvRadio;
import org.rpi.providers.PrvVolume;
import org.rpi.radio.CustomChannel;

public class PlayManager implements Observer {

	private CustomTrack current_track = null;
	private CopyOnWriteArrayList<CustomTrack> tracks = new CopyOnWriteArrayList<CustomTrack>();
	private CopyOnWriteArrayList<String> shuffled_tracks = new CopyOnWriteArrayList<String>();

	private static Logger log = Logger.getLogger(PlayManager.class);

	private IPlayer mPlayer = null;

	private boolean repeatPlayList = false;
	private boolean shuffle = false;
	private boolean bPaused;
	private PrvRadio iRadio;
	private boolean standby = true;
	//private PrvPlayList iPlayList;
	//private PrvInfo iInfo;
	//private PrvTime iTime;
	//private PrvVolume iVolume;

	// For nearly Gapless playback..
	// private long current_duration = 0;
	// private boolean bPreLoading = false;
	// private IPlayer tempPlayer = null;
	// private CustomTrack tempTrack = null;

	// For Volume
	private long volume = 100;
	private boolean bMute;
	
	//Observable Classes
	private ObservsableTime obsvTime = new ObservsableTime();
	private ObservableInfo obsvInfo = new ObservableInfo();
	private ObservableVolume obsvVolume = new ObservableVolume();
	private ObservableRadio obsvRadio = new ObservableRadio();
	private ObservablePlayList obsvPlayList = new ObservablePlayList(); 
	private ObservableProduct obsvProduct = new ObservableProduct();

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
		
	}

	/**
	 * Play This Track If MPlayer is already running destroy it
	 * 
	 * @param t
	 */
	private void playThis(CustomTrack t) {
		if (t != null) {
			current_track = t;
			log.debug("Destroy current MPlayer");
			if (mPlayer != null) {
				mPlayer.destroy();
				mPlayer = null;
			}
			mPlayer = new MPlayer();
			mPlayer.addObserver(this);
			mPlayer.playTrack(t, volume, bMute);
		}
	}

	/**
	 * Get the Next Track when not in shuffle Mode
	 * 
	 * @param offset
	 * @return
	 */
	public CustomTrack getNextTrack(int offset) {
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
	private CustomTrack getRandomTrack(int offset) {
		if (current_track != null) {
			if (!(current_track instanceof CustomChannel)) {
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
							CustomTrack newTrack = getTrackFromId(Integer.parseInt(track_id));
							return (newTrack);
						}
					} else {
						log.info("We have Reached the End of the PlayList");
						if (isRepeatPlayList()) {
							log.info("Repeat Playlsit is Set so start again...");
							shuffleTracks();
							if (getShuffledTracks().size() > 0) {
								String track_id = getShuffledTracks().get(0);
								CustomTrack newTrack = getTrackFromId(Integer.parseInt(track_id));
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
			CustomTrack t = getTrackFromId(Integer.parseInt(id));
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
	private CustomTrack getTrack(int offset) {
		if (current_track != null)
			try {
				int i = 0;
				for (CustomTrack t : getTracks()) {
					if (current_track.getId() == t.getId()) {
						break;
					}
					i++;
				}
				if (getTracks().size() > i + offset) {
					if (i + offset >= 0) {
						CustomTrack newTrack = getTracks().get(i + offset);
						return (newTrack);
					}
				} else {
					log.info("We have Reached the End of the PlayList");
					if (isRepeatPlayList()) {
						log.info("Repeat Playlsit is Set so start again...");
						if (getTracks().size() > 0) {
							CustomTrack newTrack = getTracks().get(0);
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
	public CustomTrack getTrackFromIndex(int index) {
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
	public CustomTrack getTrackFromId(int id) {
		// int i = 0;
		log.debug("GetTrakcFromId: " + id);
		for (CustomTrack t : tracks) {
			if (t.getId() == id) {
				return t;
			}
			// i++;
		}
		return null;
	}
	
	public  synchronized boolean isStandby() {
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
		//iPlayList.updateShuffle(shuffle);
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
	private void addAsNextShuffleTrack(CustomTrack t) {
		int this_track = getTrackIndexShuffled(t.getId());
		if (this_track >= 0) {
			shuffled_tracks.remove(this_track);
		}
		if (current_track != null) {
			if (!(current_track instanceof CustomChannel)) {
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
		for (CustomTrack t : tracks) {
			shuffled_tracks.add("" + t.getId());
		}
		long seed = System.nanoTime();
		Collections.shuffle(shuffled_tracks, new Random(seed));
		if (current_track != null) {
			if (current_track instanceof CustomChannel)
				return;
			log.debug("We have shuffled so set the current track to index ZERO");
			int index = getTrackIndexShuffled(current_track.getId());
			shuffled_tracks.remove(index);
			shuffled_tracks.add(0, "" + current_track.getId());
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
	private synchronized CopyOnWriteArrayList<CustomTrack> getTracks() {
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
	public synchronized void setTracks(CopyOnWriteArrayList<CustomTrack> tracks) {
		this.tracks = (CopyOnWriteArrayList<CustomTrack>) tracks.clone();
	}

	public synchronized void setCurrentTrack(CustomTrack track) {
		current_track = track;
		log.debug("Current Track Id: " + track.getId());
	}

	/**
	 * 
	 * @return
	 */
	public synchronized CustomTrack getCurrentTrack() {
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
		if (mPlayer != null) {
			mPlayer.pause(bPause);
		}
		setPaused(bPause);
		setStatus("Paused");
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
		if (mPlayer != null) {
			mPlayer.stop();
		}
	}

	/**
	 * Play track with Index Determine if shuffle is enabled, if we are not
	 * playin anything shuffle the tracks and set this track as the top track,
	 * else set this track as the top track and play
	 * 
	 * @param index
	 */
	public synchronized void playIndex(long index) {
		CustomTrack t = getTrackFromIndex((int) index);
		if (shuffle) {
			if (mPlayer == null) {
				shuffleTracks();
			} else {
				if (!mPlayer.isPlaying()) {
					shuffleTracks();
				}
			}
			addAsNextShuffleTrack(t);
		}
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
			if (mPlayer != null) {
				mPlayer.resume();
			}
			setStatus("Playing");
			setPaused(false);
		} else {
			if (shuffle)
				shuffleTracks();
			CustomTrack t = getNextTrack(1);
			if (t != null) {
				playThis(t);
			}
		}
	}

	/**
	 * Play a Radio Channel
	 * 
	 * @param c
	 */
	public synchronized void playFile(CustomChannel c) {
		log.debug("Play Radio Id:  " + c.getId());
		playThis(c);
	}

	/**
	 * Used by Alarm Plugin to Start Playing a radio channel by name
	 * 
	 * @param name
	 */
	public synchronized void playRadio(String name) {
		CustomChannel channel = iRadio.getChannel(name);
		if (channel != null)
			playFile(channel);
	}

	/***
	 * Play the Next Track
	 */
	public synchronized void nextTrack() {
		CustomTrack t = getNextTrack(1);
		if (t != null) {
			playThis(t);
		}
	}

	/***
	 * Play the Previous Track
	 */
	public synchronized void previousTrack() {
		CustomTrack t = getNextTrack(-1);
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
		if (mPlayer != null) {
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
		this.volume = volume;
		EventVolumeChanged ev = new EventVolumeChanged();
		ev.setVolume(volume);
		obsvVolume.notifyChange(ev);
		{
			if (mPlayer != null) {
				mPlayer.setVolume(volume);
			}
		}
	}

//	/**
//	 * Change and Update the Volume.
//	 * 
//	 * @param volume
//	 */
//	public synchronized void updateVolume(long volume) {
//		setVolume(volume);
//		iVolume.updateVolume(volume);
//	}

	/**
	 * Set Mute
	 * 
	 * @param mute
	 */
	public synchronized void setMute(boolean mute) {
		this.bMute = mute;
		EventMuteChanged em = new EventMuteChanged();
		em.setMute(mute);
		obsvProduct.notifyChange(em);
		obsvVolume.notifyChange(em);
		if (mPlayer != null) {
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
	public synchronized void insertTrack(long aAfterId, CustomTrack track) {
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
	private void insertAfterTrack(long aAfterId, CustomTrack track) {
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
	private void insertAfterShuffleTrack(long aAfterId, CustomTrack track) {
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
		for (CustomTrack t : getTracks()) {
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
		if (!(getCurrentTrack() instanceof CustomChannel)) {
			current_track = null;
			if (mPlayer != null) {
				mPlayer.stop();
				setStatus("Stopped");
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
		CustomTrack t = getCurrentTrack();
		if (t != null)
			if (t.getId() == iD && !(t instanceof CustomChannel)) {
				if (mPlayer != null) {
					mPlayer.stop();
				}
				setStatus("Stopped");
			}
	}

//	/**
//	 * Time provider
//	 * 
//	 * @param iTime
//	 */
//	public synchronized void setTime(PrvTime iTime) {
//		this.iTime = iTime;
//	}

//	/**
//	 * Info Provider
//	 * 
//	 * @param iInfo
//	 */
//	public synchronized void setInfo(PrvInfo iInfo) {
//		this.iInfo = iInfo;
//	}

//	/**
//	 * Playlist Provider
//	 * 
//	 * @param iPlayList
//	 */
//	public synchronized void setPlayList(PrvPlayList iPlayList) {
//		this.iPlayList = iPlayList;
//	}

	/**
	 * Radio Provider
	 * 
	 * @param iRadio
	 */
	public synchronized void setRadio(PrvRadio iRadio) {
		this.iRadio = iRadio;
	}

	/***
	 * Destroy
	 */
	public synchronized void destroy() {
		log.debug("Start of destroy");
		if (mPlayer != null) {
			log.debug("Attempt to Destroy MPlayer");
			mPlayer.destroy();
		}
	}

	/**
	 * Update the Status
	 * 
	 * @param status
	 */
	public synchronized void setStatus(String status) {
		setStatus(status, null);
	}

	/***
	 * Set the Status of the Track
	 * 
	 * @param status
	 */
	public synchronized void setStatus(String status, CustomTrack t) {
		EventTrackChanged ev = new EventTrackChanged();
		ev.setTrack(t);
		if (status.equalsIgnoreCase("PLAYING")) {
			if (t != null) {
				current_track = t;
			}
			//setInfoTrack(current_track);
			playingTrack(current_track.getId());
			ev.setTrack(current_track);

		}
		if (current_track instanceof CustomChannel) {
			//iRadio.setStatus(status);
			EventRadioStatusChanged evr = new EventRadioStatusChanged();
			evr.setStatus(status);
			obsvRadio.notifyChange(evr);
		} else {
			EventPlayListStatusChanged evr = new EventPlayListStatusChanged();
			evr.setStatus(status);
			obsvPlayList.notifyChange(evr);
			//iPlayList.SetStatus(status);
		}
		// fireEvent(ev);
		obsvInfo.notifyChange(ev);
	}

	/***
	 * Update Control Point we are playing a track
	 * 
	 * @param iD
	 */
	public synchronized void playingTrack(int iD) {
		if (current_track instanceof CustomChannel) {
			EventRadioPlayingTrackID evrp = new EventRadioPlayingTrackID();
			evrp.setId(iD);
			obsvRadio.notifyChange(evrp);
			//iRadio.playingTrack(iD);
		} else {
			EventPlayListPlayingTrackID evrp = new EventPlayListPlayingTrackID();
			evrp.setId(iD);
			obsvPlayList.notifyChange(evrp);
			//iPlayList.PlayingTrack(iD);
		}
	}

//	/***
//	 * Set the Track MetaText
//	 * 
//	 * @param metadata
//	 */
//	public synchronized void setInfoMetaData(String metadata) {
//		if (current_track != null)
//			// current_track.setMetaText(metadata);
//			iInfo.setMetaText(metadata);
//	}

//	/***
//	 * Set the Track Info
//	 * 
//	 * @param track
//	 */
//	public synchronized void setInfoTrack(CustomTrack track) {
//		if (track != null) {
//			iInfo.setTrack(track);
//		}
//	}

	/***
	 * Increase the Volume
	 * 
	 * @return
	 */
	public synchronized long incVolume() {
		if (volume < 100) {
			volume++;
			setVolume(volume);
		}
		return volume;
	}

	/***
	 * Decrease the Volume
	 * 
	 * @return
	 */
	public synchronized long decVolume() {
		if (volume > 0) {
			volume--;
			setVolume(volume);
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

	private PrvProduct iProduct = null;

	public void setProduct(PrvProduct iProduct) {
		this.iProduct = iProduct;
	}

	public void updateStandby(boolean value) {
		iProduct.updateStandby(value);
	}

	@Override
	public void update(Observable paramObservable, Object obj) {
		EventBase e = (EventBase) obj;
		switch (e.getType()) {
		case EVENTFINISHEDCURRENTTRACK:
			EventFinishedCurrentTrack evct = (EventFinishedCurrentTrack) e;
			if (evct.isQuit()) {
				log.debug("Track was Stopped, do not select Next Track");
			} else {
				log.debug("Track Stopped, get Next Track");
				CustomTrack t = getNextTrack(1);
				if (t != null) {
					playThis(t);
				}
			}
			break;
		case EVENTTIMEUPDATED:
			obsvTime.notifyChange(e);
			//notifyChange(e);
			break;
		case EVENTSTATUSCHANGED:
			EventStatusChanged es = (EventStatusChanged) e;
			setStatus(es.getStatus());
			break;
		case EVENTDURATIONUPDATE:
			obsvTime.notifyChange(e);
			break;
		case EVENTUPDATETRACKINFO:
			obsvInfo.notifyChange(e);
			break;
		case EVENTUPDATETRACKMETADATA:
			EventUpdateTrackMetaData etm = (EventUpdateTrackMetaData) e;
			String metadata = current_track.updateTrack(etm.getArtist(), etm.getTitle());
			etm.setMetaData(metadata);
			obsvInfo.notifyChange(etm);
			//if (metadata != null)
			//	setInfoMetaData(metadata);
			break;
		case EVENTLOADED:
			log.debug("Track Loaded");
			break;
		}
	}
	

	public synchronized void observTimeEvents(Observer o)
	{
		obsvTime.addObserver(o);
	}
	
	public synchronized void observInfoEvents(Observer o)
	{
		obsvInfo.addObserver(o);
	}
	
	public synchronized void observVolumeEvents(Observer o)
	{
		obsvVolume.addObserver(o);
	}
	
	public synchronized void observPlayListEvents(Observer o)
	{
		obsvPlayList.addObserver(o);
	}
	
	public synchronized void observeProductEvents(Observer o)
	{
		obsvProduct.addObserver(o);
	}
	
	public synchronized void observRadioEvents(Observer o)
	{
		obsvRadio.addObserver(o);
	}



}
