package org.rpi.java.sound;

import java.math.BigInteger;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;
import org.rpi.config.Config;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventMuteChanged;
import org.rpi.player.events.EventVolumeChanged;
import org.rpi.player.observers.ObservableVolume;
import org.rpi.utils.Utils;

public class JavaSoundPlayerLatency implements Runnable, IJavaSoundPlayer, Observer {
	private Logger log = Logger.getLogger(this.getClass());
	private boolean run = true;

	private AudioFormat audioFormat = null;
	private DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, 16000);

	private SourceDataLine soundLine = null;

	private boolean bWrite = false;

	private Vector<IAudioPacket> mWorkQueue = new Vector<IAudioPacket>();

	private float volume = 0;
	private boolean bMute = false;

	private int bitDepth = 16;

	public JavaSoundPlayerLatency() {
		volume = (float) PlayManager.getInstance().getVolume();
		if (volume != 0) {
			volume = volume / 100;
		}
		bMute = PlayManager.getInstance().getMute();
		PlayManager.getInstance().observeVolumeEvents(this);
	}

	@Override
	public void createSoundLine(AudioInformation audioInf) {
		try {
			setAudioDevice();
			bWrite = false;// Stop trying to write to the SoundLine
			if (soundLine != null) {
				close();
			}
			log.debug("Creating Audio Format: " + audioInf.toString());
			bitDepth = audioInf.getBitDepth();
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

	private void addData(IAudioPacket packet) {

		if (!bWrite ||bMute)
			return;
		try {
			if (soundLine != null) {
				switch (bitDepth) {
				case 16:
					soundLine.write(changeVolume16Bit(packet), 0, packet.getLength());
					break;
				
				case 24:
					soundLine.write(changeVolume24Bit(packet), 0, packet.getLength());
					break;
				case 32:
					soundLine.write(changeVolume32Bit(packet), 0, packet.getLength());
					break;
				}
					
			}
		} catch (Exception e) {
			int length = -99;
			if (packet != null) {
				length = packet.getLength();
			}
			log.error("Error Writing Data, Data Length: " + length, e);
		}
	}

	/*
	 * Change the Volume of a 16 bit sample
	 */
	private byte[] changeVolume16Bit(IAudioPacket packet) {
		if (volume >= 1) {
			return packet.getAudio();
		}
		byte[] audio = packet.getAudio();

		for (int i = 0; i < audio.length; i += 2) {
			// convert byte pair to int
			short buf1 = audio[i];
			short buf2 = audio[i + 1];

			buf1 = (short) ((buf1 & 0xff) << 8);
			buf2 = (short) (buf2 & 0xff);

			short res = (short) (buf1 | buf2);
			res = (short) (res * volume);

			// convert back
			audio[i + 1] = (byte) res;
			audio[i] = (byte) (res >> 8);
		}
		return audio;
	}
	
	
	private byte[] changeVolumeTest16Bit(IAudioPacket packet) {
		if (volume >= 1) {
			return packet.getAudio();
		}
		byte[] audio = packet.getAudio();

		for (int i = 0; i < audio.length; i += 2) {
			// convert byte pair to int
			byte[] data = new byte[2];
			data[0] = audio[i+0];
			data[1] = audio[i+1];

			BigInteger big = new BigInteger(data);
			float res = big.floatValue();
			res = res * volume;
			int ress = (int)res;
			// convert back
			audio[i+0] = (byte) ((ress & 0x0000FF00) >> 8);
			audio[i+1] = (byte) ((ress & 0x000000FF) >> 0);
		}
		return audio;
	}

	/*
	 * Change the Volume of a 24 bit sample
	 */
	private byte[] changeVolume24Bit(IAudioPacket packet) {
		if (volume >= 1) {
			return packet.getAudio();
		}
		byte[] audio = packet.getAudio();

		for (int i = 0; i < audio.length; i += 3) {
			// convert byte pair to int
			byte[] data = new byte[3];
			data[0] = audio[i+0];
			data[1] = audio[i+1];
			data[2] = audio[i+2];
			BigInteger big = new BigInteger(data);
			float res = big.floatValue();
			res = res * volume;
			int ress = (int)res;
			// convert back
			audio[i+0] = (byte) ((ress & 0x00FF0000) >> 16);
			audio[i+1] = (byte) ((ress & 0x0000FF00) >> 8);
			audio[i+2] = (byte) ((ress & 0x000000FF) >> 0);

		}
		return audio;
	}
	
	/*
	 * Change the Volume of a 32 bit sample
	 */
	private byte[] changeVolume32Bit(IAudioPacket packet) {
		if (volume >= 1) {
			return packet.getAudio();
		}
		byte[] audio = packet.getAudio();

		for (int i = 0; i < audio.length; i += 4) {
			byte[] data = new byte[4];
			BigInteger big = new BigInteger(data);
			float  res = big.floatValue();
			res = res * volume;
			int ress = (int)res;

			// convert back
			audio[i+0] = (byte) ((ress & 0xFF000000) >> 24);
			audio[i+0] = (byte) ((ress & 0x00FF0000) >> 16);
			audio[i+1] = (byte) ((ress & 0x0000FF00) >> 8);
			audio[i+3] = (byte) ((ress & 0x000000FF) >> 0);

		}
		return audio;
	}

	/**
	 * Get the first object out of the queue. Return null if the queue is empty.
	 */
	public synchronized IAudioPacket get() {
		IAudioPacket object = peek();
		if (object != null)
			mWorkQueue.removeElementAt(0);
		return object;
	}

	/**
	 * Peek to see if something is available.
	 */
	public IAudioPacket peek() {
		if (isEmpty())
			return null;
		return mWorkQueue.elementAt(0);
	}

	public synchronized boolean isEmpty() {
		return mWorkQueue.isEmpty();
	}

	@Override
	public void put(IAudioPacket event) {
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
					IAudioPacket audio = get();
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

	@Override
	public void update(Observable o, Object obj) {
		EventBase e = (EventBase) obj;
		switch (e.getType()) {
		case EVENTVOLUMECHANGED:
			EventVolumeChanged ev = (EventVolumeChanged) e;
			try {
				volume = (float) ev.getVolume();
				if (volume != 0) {
					volume = volume / 100;
				}
			} catch (Exception ex) {
				log.error(ex);
			}

		case EVENTMUTECHANGED:
			if (o instanceof ObservableVolume) {
				try {
					if (e instanceof EventMuteChanged) {
						EventMuteChanged evmc = (EventMuteChanged) e;
						bMute = evmc.isMute();
					}
				} catch (Exception ex) {
					log.error(e);
				}
			}
			break;
		}
	}

}
