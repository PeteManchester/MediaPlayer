package org.rpi.airplay;

import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;
import org.rpi.alacdecoder.AlacFile;
import org.rpi.config.Config;
import org.rpi.mplayer.TrackInfo;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventUpdateTrackInfo;
import org.rpi.songcast.core.AudioInformation;
import org.rpi.songcast.ohm.SourceTimer;

public class AudioEventQueue implements Runnable, Observer {
	private Logger log = Logger.getLogger(this.getClass());

	private Vector mWorkQueue = new Vector();
	private boolean run = true;
	private int frame_size = 4;
	public final int MAX_PACKET = 2048;

	// private AlacFile alac;
	//private int[] outbuffer;

	private SourceDataLine soundLine = null;
	private AudioFormat audioFormat = null;
	private DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, 16000);
	//private AlacFile alac;

	private boolean bWrite = false;

	private SourceTimer timer = null;
	private Thread timerThread = null;

	private AudioSession session = null;

	public AudioEventQueue(AudioSession session) {
		// this.session = session;
		sessionChanged();
		try {

			startTimer();

			if (soundLine != null) {
				close();
			}
			AudioSessionHolder.getInstance().addObserver(this);

			AudioInformation audioInf = new AudioInformation(44100, 48, 16, 2, "ALAC", 0, 0);
			log.debug("Creating Audio Format: " + audioInf.toString());
			audioFormat = new AudioFormat(audioInf.getSampleRate(), audioInf.getBitDepth(), audioInf.getChannels(), audioInf.isSigned(), audioInf.isBigEndian());
			info = new DataLine.Info(SourceDataLine.class, audioFormat, 16000);
			if (soundLine == null) {
				soundLine = (SourceDataLine) AudioSystem.getLine(info);
				soundLine.open(audioFormat);
				soundLine.start();
				bWrite = true;
			}
			bWrite = true;

			TrackInfo info = new TrackInfo();
			info.setBitDepth(audioInf.getBitDepth());
			info.setCodec(audioInf.getCodec());
			info.setBitrate(audioInf.getBitRate());
			info.setSampleRate((long) audioInf.getSampleRate());
			info.setDuration(0);
			EventUpdateTrackInfo ev = new EventUpdateTrackInfo();
			ev.setTrackInfo(info);
			if (ev != null) {
				PlayManager.getInstance().updateTrackInfo(ev);
			}

		} catch (Exception e) {
			log.error("Error Opening Audio Line", e);
		}
	}

	/**
	 * Used to set the Songcast Audio Device
	 */
	private void setAudioDevice() {
		Properties props = System.getProperties();
		String name = "#" + Config.getInstance().getJavaSoundcardName();
		props.setProperty("javax.sound.sampled.SourceDataLine", name);
		log.warn("###Setting Sound Card Name: " + name);
	}

	public synchronized boolean isEmpty() {
		return mWorkQueue.isEmpty();
	}

	public synchronized void put(Object object) {
		try {
			mWorkQueue.addElement(object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Get the first object out of the queue. Return null if the queue is empty.
	 */
	public synchronized Object get() {
		Object object = peek();
		if (object != null)
			mWorkQueue.removeElementAt(0);
		return object;
	}

	/**
	 * Peek to see if something is available.
	 */
	public Object peek() {
		if (isEmpty())
			return null;
		return mWorkQueue.elementAt(0);
	}

	private void sleep(int value) {
		try {
			Thread.sleep(value);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	public synchronized void clear() {
		try {
			log.info("Clearing Work Queue. Number of Items: " + mWorkQueue.size());
			mWorkQueue.clear();
			log.info("WorkQueue Cleared");
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
		}
	}

	@Override
	public void run() {
		while (run) {
			if (!isEmpty()) {
				try {
					byte[] packet = (byte[]) get();
					processEvent(packet);
				} catch (Exception e) {
					log.error(e);
				}
			} else {
				sleep(1000);
			}
		}

	}

	private void processEvent(byte[] packet) {
		try {
			int length = packet.length;
			soundLine.write(packet, 0, length / 2);
		} catch (Exception e) {
			log.error("Error processEvent", e);
		}

	}

	/**
	 * The session has changed to reset our values
	 */
	private void sessionChanged() {
		log.debug("Session Changed");
		session = AudioSessionHolder.getInstance().getSession();
		//alac = session.getAlac();
		//frame_size = session.getFrameSize();
		//outbuffer = new int[4 * (frame_size + 3)];
		setAudioDevice();
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		sessionChanged();
	}

	/**
	 * Close the SoundLine
	 */
	private void close() {
		try {
			if (soundLine != null) {
				soundLine.close();
				soundLine = null;
			}
		} catch (Exception e) {
			log.error("Error Closing Stream", e);
		}

	}

	private void startTimer() {
		timer = new SourceTimer();
		timerThread = new Thread(timer, "SongcastTimer");
		timerThread.start();
	}

	private void stopTimer() {
		if (timer != null) {
			timer.setRun(false);
			timer = null;
		}
		if (timerThread != null) {
			timerThread = null;
		}
	}

	public void stop() {
		stopTimer();
		bWrite = false;
		run = false;
		clear();
		close();
	}

}
