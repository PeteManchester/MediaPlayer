package org.rpi.mpdplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import org.apache.log4j.Logger;
import org.rpi.mplayer.TrackInfo;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventCurrentTrackFinishing;
import org.rpi.player.events.EventDurationUpdate;
import org.rpi.player.events.EventPlayListPlayingTrackID;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventVolumeChanged;

public class StatusMonitor extends Observable implements Runnable {

	private static Logger log = Logger.getLogger(StatusMonitor.class);

	private TCPConnector tcp = null;
	private boolean isRunning = true;
	private boolean sentFinishingEvent = false;

	private String current_songid = "";
	private String current_state = "";
	private String current_time = "";
	private String current_duration = "";
	private String current_title = "";
	private String current_volume = "";
	private int iCount = 0;

	private List<String> songs = new ArrayList<String>();
	private TrackInfo ti = null;

	public StatusMonitor(TCPConnector tcp) {
		this.tcp = tcp;
		ti = new TrackInfo(tcp.getiPlayer());
	}

	@Override
	public void run() {
		while (isRunning()) {
			List<String> commands = new ArrayList<String>();
			commands.add(tcp.createCommand("status"));
			commands.add(tcp.createCommand("currentsong"));
			boolean bSongChanged = false;
			HashMap<String, String> res = tcp.sendCommand(tcp.createCommandList(commands));
			String value = "";
			//log.debug(res);
			if (res.containsKey("songid")) {
				value = res.get("songid");
				if (!current_songid.equalsIgnoreCase(value)) {
					log.debug("Song Changed From : " + current_songid + " To: " + value);
					EventTrackChanged ev = new EventTrackChanged();
					ev.setMPD_id(value);
					fireEvent(ev);
					current_songid = value;
					sentFinishingEvent = false;
					bSongChanged = true;

				}
			}
			value = "";
			if (res.containsKey("state")) {
				value = res.get("state");
				if (value.equalsIgnoreCase("PLAY")) {
					value = "Playing";
				}
				if (value.equalsIgnoreCase("STOP")) {
					value = "Stopped";
				}
				if (value.equalsIgnoreCase("PAUSE")) {
					value = "Paused";
				}
				if (value != null && !current_state.equalsIgnoreCase(value)) {
					log.debug("Status Changed From : " + current_state + " To: " + value);
					current_state = value;
					tcp.getiPlayer().setStatus(value);
				} else {
					if (bSongChanged) {
						if (value != null) {
							tcp.getiPlayer().setStatus(value);
						}
					}
				}
			}

			if (res.containsKey("time")) {
				value = res.get("time");
				String[] splits = value.split(":");
				String mTime = splits[0];
				String mDuration = splits[1];
				long lDuration = 0;
				long lTime = 0;
				lDuration = Long.valueOf(mDuration).longValue();
				if (mTime != null && !current_time.equalsIgnoreCase(mTime)) {
					EventTimeUpdate e = new EventTimeUpdate();
					lTime = Long.valueOf(mTime).longValue();
					e.setTime(lTime);
					tcp.getiPlayer().fireEvent(e);
					current_time = mTime;
				}

				if (mDuration != null && !current_duration.equalsIgnoreCase(mDuration)) {
					EventDurationUpdate e = new EventDurationUpdate();
					e.setDuration(lDuration);
					tcp.getiPlayer().fireEvent(e);
					current_duration = mDuration;
				}

				if (lTime > 0 && lDuration > 0) {
					if (lDuration - lTime < 10) {
						if (!sentFinishingEvent) {
							EventCurrentTrackFinishing ev = new EventCurrentTrackFinishing();
							fireEvent(ev);
							sentFinishingEvent = true;
						}
					}
				}
			}
			
			if(res.containsKey("volume"))
			{
				String volume = res.get("volume");
				if(!current_volume.equalsIgnoreCase(volume))
				{
					EventVolumeChanged ev = new EventVolumeChanged();
					long l = Long.valueOf(volume).longValue();
					ev.setVolume(l);
					tcp.getiPlayer().fireEvent(ev);
					current_volume = volume;
				}
			}

			if (res.containsKey("Title")) {
				String full_title = res.get("Title");
				if (!current_title.equalsIgnoreCase(full_title)) {
					String artist = "";
					String title = full_title;
					try {
						String fulls[] = full_title.split("-");
						if (fulls.length > 1) {
							title = fulls[0].trim();
							artist = fulls[1].trim();
							if (artist.endsWith("'")) {
								artist = artist.substring(0, artist.length() - 1);
							}
						}
					} catch (Exception e) {

					}
					tcp.getiPlayer().updateInfo(title, artist);
					current_title = full_title;
				}
			}

			if (bSongChanged) {
				ti.setUpdated(false);
			}
			
			if(!ti.isUpdated())
			{

				if (res.containsKey("audio")) {
					String audio = res.get("audio");
					String[] splits = audio.split(":");
					try {
						String sample = splits[0];
						long sr = Long.valueOf(sample).longValue();
						ti.setSampleRate(sr);
						ti.setCodec(" ");
						long duration = Long.valueOf(current_duration).longValue();
						ti.setDuration(duration);
						if (splits.length > 1) {
							String depth = splits[1];
							long dep = Long.valueOf(depth).longValue();
							ti.setBitDepth(dep);
						}
					} catch (Exception e) {

					}
				}

				if (res.containsKey("bitrate")) {
					String bitrate = res.get("bitrate");
					try {
						long br = Long.valueOf(bitrate).longValue();
						ti.setBitrate(br);
					} catch (Exception e) {

					}
				}
				if (ti.isSet())
				{
					ti.setUpdated(true);
				}
					
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error(e);
			}
		}

	}

	private String getValue(String s, String name) {
		String value = "";
		if (s.startsWith(name)) {
			return value = s.substring(name.length(), s.length()).trim();
		}
		return null;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	private void fireEvent(EventBase ev) {
		setChanged();
		notifyObservers(ev);
	}
}
