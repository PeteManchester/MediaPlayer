package org.rpi.songcast.core;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohm.OHMEventAudio;

/**
 * SongcastPlayer that uses JavaSound
 * 
 * @author phoyle
 * 
 */

public class SongcastPlayerJavaSound implements ISongcastPlayer, Runnable {

	private Logger log = Logger.getLogger(this.getClass());
	private boolean run = true;

	private AudioFormat audioFormat = null;
	private DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, 16000);

	private SourceDataLine soundLine = null;

	private boolean bWrite = false;

	public SongcastPlayerJavaSound() {

	}

	public void createSoundLine(AudioInformation audioInf) {
		try {
			log.info("Creating Audio Format: " + audioInf.toString());
			audioFormat = new AudioFormat(audioInf.getSampleRate(), audioInf.getBitDepth(), audioInf.getChannels(), audioInf.isSigned(), audioInf.isBigEndian());
			log.debug("AudioFormat: " + audioFormat.toString());
			info = new DataLine.Info(SourceDataLine.class, audioFormat, 16000);
			if (soundLine == null) {
				soundLine = (SourceDataLine) AudioSystem.getLine(info);
				soundLine.open(audioFormat);
				soundLine.start();
				bWrite = true;
			}
		} catch (Exception e) {
			log.error("Error opening Sound:", e);
		}
	}

	private void close() {
		try {
			if (soundLine != null) {
				soundLine.close();
				soundLine = null;
				bWrite = false;
			}
		} catch (Exception e) {
			log.error("Error Closing Stream", e);
		}

	}

	@Override
	public void run() {
		while (run) {
			sleep(100);
		}

	}

	private void sleep(int value) {
		try {
			Thread.sleep(value);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void play() {

	}

	@Override
	public void stop() {
		run = false;
		close();
	}

	@Override
	public void put(OHMEventAudio event) {
		if (!bWrite)
			return;
		try {
			if (soundLine != null) {
				// log.debug(data.length);
				soundLine.write(event.getSound(), 0, event.getSound().length);
			}
		} catch (Exception e) {
			log.error("Error Writing Data", e);
		}

	}

}
