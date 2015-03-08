package org.rpi.java.sound;

import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

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

public class JavaSoundPlayerBasic implements Runnable, IJavaSoundPlayer, Observer {

	private Logger log = Logger.getLogger(this.getClass());
	private boolean run = true;

	private AudioFormat audioFormat = null;
	private DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, 16000);

	private SourceDataLine soundLine = null;

	private boolean bWrite = false;
	
	private double volume = 0;
	private boolean bMute = false;;

	public JavaSoundPlayerBasic() {
		volume = (double) PlayManager.getInstance().getVolume();
		if (volume != 0) {
			volume = volume / 100;
		}
		bMute = PlayManager.getInstance().getMute();
		PlayManager.getInstance().observeVolumeEvents(this);
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

	public void put(IAudioPacket event) {
		if (!bWrite) {
			event = null;
			return;
		}
		try {
			if (soundLine != null) {
				soundLine.write(changeVolume(event), 0, event.getLength());
				event = null;
			}
		} catch (Exception e) {
			log.error("Error Writing Data", e);
		}

	}
	
	private byte[] changeVolume(IAudioPacket packet) {
		if(volume >= 100)
		{
			return packet.getAudio();
		}
		byte[] audio = packet.getAudio();

		for (int i = 0; i < audio.length; i++) {
			try {
				double d = (double) ((int) audio[i]);
				d = d * volume;
				if(bMute)
				{
					d = 0;
				}
				audio[i] = (byte) ((int) d);
			} catch (Exception e) {
				log.error(e);
			}
		}
		return audio;
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

	@Override
	public void update(Observable o, Object obj) {
		EventBase e = (EventBase) obj;
		switch (e.getType()) {
		case EVENTVOLUMECHANGED:
			EventVolumeChanged ev = (EventVolumeChanged) e;
			try {
				volume = (double) ev.getVolume();
				if (volume != 0) {
					volume = volume / 100;
				}
			} catch (Exception ex) {
				log.error(ex);
			}

		case EVENTMUTECHANGED:
			if (o instanceof ObservableVolume) {
				try {
					if(e instanceof EventMuteChanged)
					{
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