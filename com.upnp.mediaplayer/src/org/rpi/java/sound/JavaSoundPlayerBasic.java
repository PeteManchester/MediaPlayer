package org.rpi.java.sound;

import java.util.Properties;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;
import org.rpi.config.Config;
import org.rpi.utils.Utils;
import org.scratchpad.songcast.core.AudioInformation;

public class JavaSoundPlayerBasic implements Runnable, IJavaSoundPlayer {

	private Logger log = Logger.getLogger(this.getClass());
	private boolean run = true;

	private AudioFormat audioFormat = null;
	private DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, 16000);

	private SourceDataLine soundLine = null;

	private boolean bWrite = false;

	public JavaSoundPlayerBasic() {
	}

	public void createSoundLine(AudioInformation audioInf) {
		try {
			setAudioDevice();
			bWrite = false;// Stop trying to write to the SoundLine
			if (soundLine != null) {
				close();
			}
			log.debug("Creating Audio Format: " + audioInf.toString());
			audioFormat = new AudioFormat(audioInf.getSampleRate(), audioInf.getBitDepth(), audioInf.getChannels(), audioInf.isSigned(), audioInf.isBigEndian());
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
				bWrite = false;
				soundLine.close();
				soundLine = null;
			}
		} catch (Exception e) {
			log.error("Error Closing Stream", e);
		}

	}

	private void sleep(int value) {
		try {
			Thread.sleep(value);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	// @Override
	public void stop() {
		run = false;
		close();
	}

	public void put(AudioPacket event) {
		if (!bWrite) {
			event = null;
			return;
		}
		try {
			if (soundLine != null) {
				soundLine.write(event.getAudio(), 0, event.getLength());
				event = null;
			}
		} catch (Exception e) {
			log.error("Error Writing Data", e);
		}

	}

	@Override
	public void run() {
		while (run) {
			sleep(100);
		}
	}

	/**
	 * Used to set the Songcast Audio Device
	 */
	private void setAudioDevice() {
		Properties props = System.getProperties();
		String name = Config.getInstance().getJavaSoundcardName();
		if (!Utils.isEmpty(name)) {
			props.setProperty("javax.sound.sampled.SourceDataLine", name);
			log.warn("###Setting Sound Card Name: " + name);
		}
	}

	@Override
	public void clear() {
	}

}