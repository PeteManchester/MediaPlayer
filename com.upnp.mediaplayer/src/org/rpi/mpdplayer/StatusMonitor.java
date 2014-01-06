package org.rpi.mpdplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.config.Config;
import org.rpi.mplayer.TrackInfo;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventCurrentTrackFinishing;
import org.rpi.player.events.EventDurationUpdate;
import org.rpi.player.events.EventStatusChanged;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.player.events.EventVolumeChanged;

public class StatusMonitor extends Observable implements Runnable, Observer {

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
	private TrackInfo ti = null;

	public StatusMonitor(TCPConnector tcp) {
		this.tcp = tcp;
		ti = new TrackInfo();
		ti.addObserver(this);
	}

	@Override
	public void run() {
		int iCount = 0;
		while (isRunning()) {
			List<String> commands = new ArrayList<String>();
			commands.add(tcp.createCommand("status"));
			commands.add(tcp.createCommand("currentsong"));
			boolean bSongChanged = false;
			HashMap<String, String> res = tcp.sendCommand(tcp.createCommandList(commands));
			String value = "";
			value = res.get("songid");
			if (value != null && !current_songid.equalsIgnoreCase(value)) {
				log.debug("Song Changed From : " + current_songid + " To: " + value);
				EventTrackChanged ev = new EventTrackChanged();
				ev.setMPD_id(value);
				fireEvent(ev);
				current_songid = value;
				sentFinishingEvent = false;
				bSongChanged = true;

			}
			value = res.get("state");
			if (value != null) {
				if (value.equalsIgnoreCase("PLAY")) {
					value = "Playing";
				} else if (value.equalsIgnoreCase("STOP")) {
					value = "Stopped";
				} else if (value.equalsIgnoreCase("PAUSE")) {
					value = "Paused";
				}
				if (value != null && !current_state.equalsIgnoreCase(value)) {
					log.debug("Status Changed From : " + current_state + " To: " + value);
					current_state = value;
					setStatus(value);
				} else {
					if (bSongChanged) {
						if (value != null) {
							setStatus(value);
						}
					}
				}
			}
			value = res.get("time");
			if(value !=null)
			{
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
					fireEvent(e);
					current_time = mTime;
				}

				if (mDuration != null && !current_duration.equalsIgnoreCase(mDuration)) {
					EventDurationUpdate e = new EventDurationUpdate();
					e.setDuration(lDuration);
					fireEvent(e);
					current_duration = mDuration;
				}

				if (lTime > 0 && lDuration > 0) {
					if ((lDuration - lTime) < Config.mpd_preload_timer) {
						if (!sentFinishingEvent) {
							EventCurrentTrackFinishing ev = new EventCurrentTrackFinishing();
							fireEvent(ev);
							sentFinishingEvent = true;
						}
					}
				}
			}
				String volume = res.get("volume");
				if(volume !=null)
				{
				if (!current_volume.equalsIgnoreCase(volume)) {
					EventVolumeChanged ev = new EventVolumeChanged();
					long l = Long.valueOf(volume).longValue();
					ev.setVolume(l);
					fireEvent(ev);
					current_volume = volume;
				}
				}
				String full_title = res.get("Title");
				if (full_title !=null && !current_title.equalsIgnoreCase(full_title)) {
					String artist = "";
					String title = full_title;
					try {
						String fulls[] = full_title.split("-");
						if (fulls.length > 1) {
							title = fulls[1].trim();
							artist = fulls[0].trim();
							if (title.endsWith("'")) {
								title = title.substring(0, title.length() - 1);
							}
						}
					} catch (Exception e) {

					}
					EventUpdateTrackMetaText ev = new EventUpdateTrackMetaText();
					ev.setArtist(artist);
					ev.setTitle(title);
					fireEvent(ev);
					current_title = full_title;
				}

			if (bSongChanged) {
				ti.setUpdated(false);
			}
			String audio = res.get("audio");
			if(audio !=null)
			{
				if (!ti.isUpdated() || iCount > 5) {

					if (iCount > 5) {
						ti.setUpdated(false);
						iCount = 0;
					}
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
							ti.setCodec("" + dep + " bits");
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
				if (ti.isSet()) {
					ti.setUpdated(true);
				}
				iCount++;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error(e);
			}
		}

	}


	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public synchronized void setStatus(String value) {
		EventStatusChanged ev = new EventStatusChanged();
		ev.setStatus(value);
		fireEvent(ev);
	}

	private void fireEvent(EventBase ev) {
		setChanged();
		notifyObservers(ev);
	}

	@Override
	public void update(Observable o, Object evt) {
		EventBase e = (EventBase) evt;
		fireEvent(e);
	}
}
