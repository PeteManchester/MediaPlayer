package org.rpi.mplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.rpi.player.events.EventDurationUpdate;
import org.rpi.player.events.EventFinishedCurrentTrack;
import org.rpi.player.events.EventStatusChanged;
import org.rpi.player.events.EventTimeUpdate;

public class OutputReader extends Thread {

	private static Logger log = Logger.getLogger(OutputReader.class);

	private static final Pattern END_PATTERN = Pattern.compile(".*\\x2e\\x2e\\x2e\\x20\\(.*\\x20.*\\).*");

	private Process process = null;
	private MPlayer mPlayer = null;

	private boolean readStopped;

	private boolean bUpdate = true;

	public OutputReader(MPlayer mPlayer) {
		this.setName("OR-" + mPlayer.getUniqueId());
		this.mPlayer = mPlayer;
		this.process = mPlayer.getProcess();
	}

	private void read(String line) {
		boolean print = true;
		//log.debug(line);
		if (line.startsWith("Starting playback...")) {
			mPlayer.playingTrack();
		}
		
		else if(line.startsWith("Cache fill:"))
		{
			EventStatusChanged ev = new EventStatusChanged();
			ev.setStatus("Buffering");
			mPlayer.fireEvent(ev);
		}

		else if (line.startsWith("ANS_LENGTH")) {
			String lengths[] = line.split("=");
			if (lengths.length > 1) {
				long l = stringToLong(lengths[1]);
				if (l != -99) {
					EventDurationUpdate e = new EventDurationUpdate();
					e.setDuration(l);
					mPlayer.fireEvent(e);
				}
				mPlayer.getTrackInfo().setDuration(l);
			}
			print = false;
		}

		else if (line.contains("=====  PAUSE  =====")) {
			log.debug("Paused");
		}

		else if (line.startsWith("ANS_TIME_POSITION")) {
			String lengths[] = line.split("=");
			if (lengths.length > 1) {
				EventTimeUpdate ev = new EventTimeUpdate();
				ev.setTime(stringToLong(lengths[1]));
				mPlayer.fireEvent(ev);
			}
			print = false;
		}

		else if (line.startsWith("ANS_AUDIO_BITRATE")) {
			mPlayer.getTrackInfo().setBitrate(stringToLong(line));
			print = false;
		}

		else if (line.startsWith("ANS_AUDIO_CODEC")) {
			String[] splits = line.split("=");
			if (splits.length == 2) {
				String codec = splits[1];
				codec = codec.replaceAll("[']", "");
				mPlayer.getTrackInfo().setCodec(codec);
			}

			print = false;
		}

		else if (line.startsWith("ANS_AUDIO_SAMPLES")) {
			String[] splits = line.split(",");
			if (splits.length == 2) {
				mPlayer.getTrackInfo().setSampleRate(stringToLong(splits[0]));
				print = false;
			}
		}

		else if (line.startsWith("File not found")) {
			// Stop output reader
			readStopped = true;
			log.error("FILE NOT FOUND");
		}

		else if (line.startsWith("Exiting... (Quit)")) {
			if (isUpdate()) {
				mPlayer.setStatus("Stopped");
			}
			EventFinishedCurrentTrack ev = new EventFinishedCurrentTrack();
			ev.setQuit(true);
			mPlayer.fireEvent(ev);
		}

		// Get Track info for Radio Streams.
		else if (line.startsWith("ICY")) {
			log.debug("ICY INFO: " + line);
			print = false;
			try {
				String temp = line.substring(line.toUpperCase().indexOf("STREAMTITLE='") + 13);
				if (temp.length() > 0) {
					String[] splits = temp.split(";");
					if (splits.length > 1) {
						String[] fulls = splits[0].split("-");
						if (fulls.length >1) {
							String title = fulls[1].trim();
							String artist = fulls[0].trim();
							if (title.endsWith("'")) {
								title = title.substring(0, title.length() - 1);
							}
							//log.debug("ICY IFNO Artist is: " + artist + " Title is: " + title);
							mPlayer.updateInfo(artist, title);
						}
						else
						{
							//Just for Absolute radio who decided to use '~' as a seperator 
							String[] abs = splits[0].split("~");
							if(abs.length>1)
							{
								String artist = abs[0].trim();
								String title = abs[1].trim();
								if (artist.endsWith("'")) {
									artist = artist.substring(0, artist.length() - 1);
								}
								//log.debug("ICY IFNO Artist is: " + artist + " Title is: " + title);
								mPlayer.updateInfo(artist, title);
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("Whoops, error handling ICY Info", e);
			}
		}

		// End
		else if (END_PATTERN.matcher(line).matches() && !this.readStopped) {
			log.debug("Finished");
			mPlayer.stoppedPlaying();
		}
		

		if (print) {
			if (!line.trim().equals("")) {
				log.debug(line);
			}
		}

	}

	/***
	 * Converts a String With decimal places to a long
	 * 
	 * @param s
	 * @return
	 */
	private long stringToLong(String s) {
		s = s.replaceAll("[^.0-9\\s]", "");
		NumberFormat nf = NumberFormat.getInstance();
		Number number;
		try {
			number = nf.parse(s);
			long l = number.longValue();
			return l;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -99;
	}

	@Override
	public void run() {
		String line = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
		try {
			line = in.readLine();
			while (line != null && (mPlayer.isPlaying() || mPlayer.isLoading()) && !isInterrupted()) {
				read(line);
				line = in.readLine();
			}
		} catch (final IOException e) {
			log.error("Error OutputReader", e);
		} finally {
			log.debug("Closing BufferedReader");
			CloseMe.close(in);
			mPlayer.endPositionThread();
		}
	}

	/**
	 * @return the bUpdate
	 */
	private boolean isUpdate() {
		return bUpdate;
	}

	/**
	 * @param bUpdate the bUpdate to set
	 */
//	private void setUpdate(boolean bUpdate) {
//		this.bUpdate = bUpdate;
//	}

}
