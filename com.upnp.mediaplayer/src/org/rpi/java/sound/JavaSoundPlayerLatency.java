package org.rpi.java.sound;

import java.util.Properties;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;
import org.rpi.config.Config;
import org.rpi.utils.Utils;
import org.scratchpad.songcast.core.AudioInformation;

public class JavaSoundPlayerLatency implements Runnable, IJavaSoundPlayer {
	private Logger log = Logger.getLogger(this.getClass());
	private boolean run = true;

	private AudioFormat audioFormat = null;
	private DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, 16000);

	private SourceDataLine soundLine = null;

	private boolean bWrite = false;

	private Vector<AudioPacket> mWorkQueue = new Vector<AudioPacket>();

	@Override
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

	private void addData(AudioPacket packet) {

		if (!bWrite)
			return;
		try {
			if (soundLine != null) {
				soundLine.write(packet.getAudio(), 0, packet.getLength());
			}
		} catch (Exception e) {
			int length = -99;
			if (packet != null) {
				length = packet.getLength();
			}
			log.error("Error Writing Data, Data Length: " + length, e);
		}
	}

	/**
	 * Get the first object out of the queue. Return null if the queue is empty.
	 */
	public synchronized AudioPacket get() {
		AudioPacket object = peek();
		if (object != null)
			mWorkQueue.removeElementAt(0);
		return object;
	}

	/**
	 * Peek to see if something is available.
	 */
	public AudioPacket peek() {
		if (isEmpty())
			return null;
		return mWorkQueue.elementAt(0);
	}

	public synchronized boolean isEmpty() {
		return mWorkQueue.isEmpty();
	}

	@Override
	public void put(AudioPacket event) {
		try {
			mWorkQueue.addElement(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	

	@Override
	public void stop() {
		run = false;
		clear();
		close();
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
					AudioPacket audio = get();
					while (audio.getTimeToPlay() > System.currentTimeMillis()) {
						if (audio.expired()) {
							break;
						}
						sleep(1);
						audio.incAttempts();
					}
					addData(audio);
				} catch (Exception e) {
					log.error("Error in Run Method");
				}
			} else {
				sleep(10);
			}
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

}
