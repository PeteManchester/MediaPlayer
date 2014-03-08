package org.rpi.providers;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgPlaylist1;
import org.rpi.channel.ChannelPlayList;
import org.rpi.config.Config;
import org.rpi.player.CommandTracker;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventPlayListPlayingTrackID;
import org.rpi.player.events.EventPlayListStatusChanged;
import org.rpi.player.events.EventPlayListUpdateShuffle;
import org.rpi.playlist.PlayListReader;
import org.rpi.playlist.PlayListWriter;
import org.rpi.utils.Utils;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;

public class PrvPlayList extends DvProviderAvOpenhomeOrgPlaylist1 implements Observer, IDisposableDevice {

	private Logger log = Logger.getLogger(PrvPlayList.class);
	private int next_id;
	private PlayListWriter plw = null;
	private int playlist_max = Config.playlist_max;
	private CommandTracker tracker = new CommandTracker();

	private CopyOnWriteArrayList<ChannelPlayList> tracks = new CopyOnWriteArrayList<ChannelPlayList>();

	private PlayManager iPlayer = PlayManager.getInstance();

	public PrvPlayList(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating CustomPlayList");

		plw = new PlayListWriter();
		plw.start();
		enablePropertyTransportState();
		enablePropertyRepeat();
		enablePropertyShuffle();
		enablePropertyId();
		enablePropertyTracksMax();
		enablePropertyProtocolInfo();
		enablePropertyIdArray();

		byte[] array = new byte[0];
		setPropertyId(0);
		setPropertyProtocolInfo(Config.getProtocolInfo());
		setPropertyRepeat(false);
		setPropertyShuffle(false);
		setPropertyTracksMax(playlist_max);
		setPropertyTransportState("");
		setPropertyIdArray(array);

		enableActionPlay();
		enableActionPause();
		enableActionStop();
		enableActionNext();
		enableActionPrevious();
		enableActionSetRepeat();
		enableActionRepeat();
		enableActionSetShuffle();
		enableActionShuffle();
		enableActionSeekSecondAbsolute();
		enableActionSeekSecondRelative();
		enableActionSeekId();
		enableActionSeekIndex();
		enableActionTransportState();
		enableActionId();
		enableActionRead();
		enableActionReadList();
		enableActionInsert();
		enableActionDeleteId();
		enableActionDeleteAll();
		enableActionTracksMax();
		enableActionIdArray();
		enableActionIdArrayChanged();
		enableActionProtocolInfo();
		PlayManager.getInstance().observePlayListEvents(this);
		loadPlayList();
	}

	private void loadPlayList() {
		if (Config.save_local_playlist) {
			PlayListReader plr = new PlayListReader(this);
			plr.getXML();
		}
	}

	protected void pause(IDvInvocation paramIDvInvocation) {
		log.debug("Pause" + Utils.getLogText(paramIDvInvocation));
		iPlayer.pause(true);
	};

	protected void play(IDvInvocation paramIDvInvocation) {
		log.debug("Play" + Utils.getLogText(paramIDvInvocation));
		if (tracker.setRequest("PLAY")) {
			iPlayer.play();
		}
	};

	protected void stop(IDvInvocation paramIDvInvocation) {
		log.debug("Stop" + Utils.getLogText(paramIDvInvocation));
		iPlayer.stop();
	};

	/***
	 * Returns the track Id
	 * 
	 * @return
	 */
	public int getNext_id() {
		next_id++;
		log.debug("GetNextId: " + next_id);
		return next_id;
	}

	/***
	 * If reading the playList from the xml file, make sure that the nextId is
	 * set to the max_id of the .xml entry..
	 * 
	 * @param max_id
	 */
	public void setNextId(int max_id) {
		next_id = max_id;

	}

	protected long insert(IDvInvocation paramIDvInvocation, long aAfterId, String aUri, String aMetaData) {
		if (tracks.size() >= playlist_max) {
			log.error("Maximum Size of PlayList Reached...");
			return -1;
		}
		log.debug("Insert After: " + aAfterId + " URI: " + aUri + " MetaDate: \r\n" + aMetaData + Utils.getLogText(paramIDvInvocation));
		int id = getNext_id();
		ChannelPlayList track = new ChannelPlayList(aUri, aMetaData, id);
		int iCount = 0;
		if (aAfterId != 0) {
			for (ChannelPlayList t : tracks) {
				if (t.getId() == aAfterId) {
					iCount++;
					break;
				}
				iCount++;
			}
		}
		try {
			tracks.add(iCount, track);
		} catch (Exception e) {
			log.error("Error Adding Track: " + track.getId() + " After: " + aAfterId, e);
		}
		iPlayer.insertTrack(aAfterId, track);
		UpdateIdArray();
		log.debug("Insert Track Return: " + id);
		return id;
	};

	protected void deleteAll(IDvInvocation paramIDvInvocation) {
		log.debug("DeleteAll" + Utils.getLogText(paramIDvInvocation));
		tracks.clear();
		UpdateIdArray();
		iPlayer.deleteAllTracks();
	};

	protected void deleteId(IDvInvocation paramIDvInvocation, long iD) {
		log.debug("DeleteId: " + iD + Utils.getLogText(paramIDvInvocation));
		int iCount = 0;
		boolean found = false;
		for (ChannelPlayList t : tracks) {
			if (t.getId() == iD) {
				found = true;
				break;
			}
			iCount++;
		}
		if (found) {
			try {
				log.debug("Deleteing Id: " + iD + " at Position : " + iCount + " In List");
				tracks.remove(iCount);
				iPlayer.DeleteTrack(iD);
			} catch (Exception e) {
				log.error("Unable to Delete Track Id: " + iD + " At List Postion : " + iCount, e);
			}
		}
		UpdateIdArray();
	};

	protected long id(IDvInvocation paramIDvInvocation) {
		log.debug("GetId" + Utils.getLogText(paramIDvInvocation));
		long id = getPropertyId();
		return id;
	};

	protected String protocolInfo(IDvInvocation paramIDvInvocation) {
		String protocolInfo = getPropertyProtocolInfo();
		log.debug("GetProtocolInfo: \r\n" + protocolInfo + Utils.getLogText(paramIDvInvocation));
		return protocolInfo;
	};

	protected IdArray idArray(IDvInvocation paramIDvInvocation) {
		log.debug("GetIdArray" + Utils.getLogText(paramIDvInvocation));
		byte[] array = getPropertyIdArray();
		DvProviderAvOpenhomeOrgPlaylist1.IdArray idArray = new IdArray(0, array);
		return idArray;
	};

	protected boolean idArrayChanged(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("GetIdArrayChanged" + Utils.getLogText(paramIDvInvocation));
		boolean changed = false;
		return changed;
	};

	protected void next(IDvInvocation paramIDvInvocation) {
		log.debug("Next" + Utils.getLogText(paramIDvInvocation));
		iPlayer.nextTrack();
	};

	protected void previous(IDvInvocation paramIDvInvocation) {
		log.debug("Previous" + Utils.getLogText(paramIDvInvocation));
		iPlayer.previousTrack();
	};

	protected Read read(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Read Index: " + paramLong + Utils.getLogText(paramIDvInvocation));
		try {
			for (ChannelPlayList t : tracks) {
				if (t.getId() == paramLong) {
					DvProviderAvOpenhomeOrgPlaylist1.Read read = new Read(t.getUri(), t.getMetadata());
					return read;
				}
			}
		} catch (Exception e) {
			log.error("Error read Index: " + paramLong, e);
		}
		DvProviderAvOpenhomeOrgPlaylist1.Read read = new Read("", "");
		return read;
	};

	protected String readList(IDvInvocation paramIDvInvocation, String ids) {

		log.debug("ReadList: " + ids + Utils.getLogText(paramIDvInvocation));
		return getList(ids);
	};

	protected boolean repeat(IDvInvocation paramIDvInvocation) {
		boolean repeat = getPropertyRepeat();
		log.debug("Repeat: " + repeat + Utils.getLogText(paramIDvInvocation));
		return repeat;
	};

	protected void seekId(IDvInvocation paramIDvInvocation, long id) {
		log.debug("SeekId: " + id + Utils.getLogText(paramIDvInvocation));
		tracker.setRequest("SEEKID");
		iPlayer.playTrackId(id);
	};

	protected void seekIndex(IDvInvocation paramIDvInvocation, long id) {
		log.debug("SeekIndex: " + id + Utils.getLogText(paramIDvInvocation));
		tracker.setRequest("SEEKINDEX");
		iPlayer.playIndex(id);
	};

	protected void seekSecondAbsolute(IDvInvocation paramIDvInvocation, long seconds) {
		log.debug("SeekSecondAbsolute: " + seconds + Utils.getLogText(paramIDvInvocation));
		iPlayer.seekAbsolute(seconds);
	};

	protected void seekSecondRelative(IDvInvocation paramIDvInvocation, int paramInt) {
		log.debug("SeekSecondRelative: " + paramInt + Utils.getLogText(paramIDvInvocation));
	};

	protected void setRepeat(IDvInvocation paramIDvInvocation, boolean repeat) {
		log.debug("SetRepeat: " + repeat + Utils.getLogText(paramIDvInvocation));
		setPropertyRepeat(repeat);
		iPlayer.setRepeatPlayList(repeat);
	};

	protected void setShuffle(IDvInvocation paramIDvInvocation, boolean paramBoolean) {
		log.debug("SetShuffle: " + paramBoolean + Utils.getLogText(paramIDvInvocation));
		setPropertyShuffle(paramBoolean);
		iPlayer.setShuffle(paramBoolean);
	};

	protected boolean shuffle(IDvInvocation paramIDvInvocation) {
		boolean shuffle = getPropertyShuffle();
		log.debug("GetShuffle: " + shuffle + Utils.getLogText(paramIDvInvocation));
		return shuffle;
	};

	protected long tracksMax(IDvInvocation paramIDvInvocation) {
		long tracksMax = getPropertyTracksMax();
		log.debug("GetTracksMax: " + tracksMax + Utils.getLogText(paramIDvInvocation));
		return tracksMax;
	};

	protected String transportState(IDvInvocation paramIDvInvocation) {
		String state = getPropertyTransportState();
		log.debug("TransportState: " + state + Utils.getLogText(paramIDvInvocation));
		return state;
	};

	private synchronized void UpdateIdArray(boolean bUpdateFile) {
		int size = tracks.size() * 4;
		StringBuilder sb = new StringBuilder();
		byte[] bytes = new byte[size];
		for (ChannelPlayList t : tracks) {
			try {
				int intValue = (int) t.getId();
				String binValue = Integer.toBinaryString(intValue);
				binValue = padLeft(binValue, 32, '0');
				sb.append(binValue);
			} catch (Exception e) {
				log.error(e);
			}
		}
		// Now we have a big long string of binary, chop it up and get the
		// bytes for the byte array..
		String myBytes = sb.toString();
		int numOfBytes = myBytes.length() / 8;
		bytes = new byte[numOfBytes];
		try

		{
			for (int i = 0; i < numOfBytes; ++i) {
				int index = 8 * i;
				String sByte = myBytes.substring(index, index + 8);
				// try {
				// log.debug("Byte: " + sByte);
				Integer x = Integer.parseInt(sByte, 2);
				Byte sens = (byte) x.intValue();
				// byte b = Byte.parseByte(sByte, 2);
				bytes[i] = sens;
				// } catch (Exception e) {
				// log.error("Error parseByte: " + sByte , e);
				// }

			}
			//log.debug("UpdateIdArray: " + sb.toString());
			setPropertyIdArray(bytes);
			if (bUpdateFile) {
				plw.trigger(tracks);
			}
		} catch (Exception e) {
			log.error("Error Writing Bytes: " + sb.toString(), e);
		}

	}

	/***
	 * Itarate all tracks, and create a 32 bit binary number from the track Id.
	 * Add the 32 bit binary string to a long string Split the 32 bit binary
	 * long string 4 bytes (8bits) And add to a byte array
	 */
	private synchronized void UpdateIdArray() {
		UpdateIdArray(true);
	}

	private String padLeft(String str, int length, char padChar) {
		StringBuilder sb = new StringBuilder();

		for (int toPrepend = length - str.length(); toPrepend > 0; toPrepend--) {
			sb.append(padChar);
		}
		sb.append(str);
		return sb.toString();
	}

	// private boolean PauseTrack() {
	// iPlayer.pause(true);
	// return true;
	// }

	private void playingTrack(int iD) {
		setPropertyId(iD);
	}

	public synchronized void setTracks(CopyOnWriteArrayList<ChannelPlayList> tracks) {
		this.tracks = tracks;
		iPlayer.setTracks(tracks);
		UpdateIdArray(false);
	}

	public void setStatus(String status) {
		setPropertyTransportState(status);
	}

	private String getList(String ids) {
		int i = 0;
		HashMap<String, String> trackIds = new HashMap<String,String>();
		for(String key:ids.split(" "))
		{
			trackIds.put(key, key);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<TrackList>");
		for (ChannelPlayList t : tracks) {
			if (trackIds.containsKey("" + t.getId())) {
				i++;
				sb.append(t.getFullText());
			}
		}
		sb.append("</TrackList>");
		log.debug("ReadList Contains : " + i + "  " + sb.toString());
		return sb.toString();
	}

	@Override
	public void dispose() {
		plw = null;
		super.dispose();
	}

	public void updateShuffle(boolean shuffle) {
		setPropertyShuffle(shuffle);
	}

	@Override
	public void update(Observable o, Object arg) {
		EventBase e = (EventBase) arg;
		switch (e.getType()) {

		case EVENTPLAYLISTSTATUSCHANGED:
			EventPlayListStatusChanged ers = (EventPlayListStatusChanged) e;
			setStatus(ers.getStatus());
			break;

		case EVENTPLAYLISTPLAYINGTRACKID:
			EventPlayListPlayingTrackID eri = (EventPlayListPlayingTrackID) e;
			playingTrack(eri.getId());
			break;

		case EVENTPLAYLISTUPDATESHUFFLE:
			EventPlayListUpdateShuffle eps = (EventPlayListUpdateShuffle) e;
			updateShuffle(eps.isShuffle());
			break;

		}

	}

    @Override
    public String getName() {
        return "PlayList";
    }

}
