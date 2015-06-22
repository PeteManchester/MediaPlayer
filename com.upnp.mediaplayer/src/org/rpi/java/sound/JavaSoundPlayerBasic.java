package org.rpi.java.sound;

import java.math.BigInteger;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelAirPlay;
import org.rpi.config.Config;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventAirplayVolumeChanged;
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
	
	private float volume = 0;
	private long airplayVolume = 0;
	private long mastervolume = 0;
	private boolean bMute = false;
	private boolean sotware_mixer_enabled = false;
	private boolean isAirplay = false;
	
	private int bitDepth = 16;

	public JavaSoundPlayerBasic() {
		mastervolume = PlayManager.getInstance().getVolume();
		airplayVolume = PlayManager.getInstance().getAirplayVolume();
		volume = calculateVolume();
		setVolume(volume);
		bMute = PlayManager.getInstance().getMute();
		PlayManager.getInstance().observeVolumeEvents(this);
		PlayManager.getInstance().observeAirplayVolumeEvents(this);
		if(PlayManager.getInstance().getCurrentTrack() instanceof ChannelAirPlay )
		{
			isAirplay = true;
		}
	}

	public void createSoundLine(AudioInformation audioInf) {
		try {
			setAudioDevice();
			bWrite = false;// Stop trying to write to the SoundLine
			if (soundLine != null) {
				close();
			}
			//audioInf.setBitDepth(8);
			log.debug("Creating Audio Format: " + audioInf.toString());
			bitDepth = audioInf.getBitDepth();
			sotware_mixer_enabled = Config.getInstance().isSoftwareMixerEnabled();
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

	public void put(IAudioPacket packet) {
		if (!bWrite) {
			packet = null;
			return;
		}
		try {
			if (soundLine != null) {
				switch (bitDepth) {
				case 8:
					byte[] array = change16BitTo8Bit(packet);
					soundLine.write(array, 0, packet.getLength()/2);
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
			log.error("Error Writing Data", e);
		}

	}
	
	private byte[] change16BitTo8Bit(IAudioPacket packet)
	{
		byte[] audio = packet.getAudio();
		int len = audio.length;
		byte[] bit = new byte[len/2];
		int tempint;
		
		for (int i=1, j=0; i<len; i+=2, j++){
			//tempint = ((int) audio[i]);// ^ 0x00000080;
			//bit[j] = (byte) tempint; 
			bit[j] = (byte)(audio[i] );
		     

		}
		//byte[] audio = packet.getAudio();
//		int length = audio.length/2;
//		byte[] bit = new byte[length];
//		int iCount = 0;
//		for (int i = 0; i < audio.length; i += 2) {
//			// convert byte pair to int
//			short buf1 = audio[i];
//			short buf2 = audio[i + 1];
//
//			buf1 = (short) ((buf1 & 0xff) << 8);
//			buf2 = (short) (buf2 & 0xff);
//
//			short res = (short) (buf1 | buf2);
//			//res = (short) (res * volume);
//			//log.debug("insert in Array: " + iCount + " Value: " + res +" Length: " + audio.length);
//			// convert back
//			int why = res >>8;
//			bit[iCount] = (byte)(why);
//			iCount++;
//		}
		return audio;
	}
	
	/*
	 * Change the Volume of a 16 bit sample
	 */
	private byte[] changeVolume16Bit(IAudioPacket packet) {
		if (volume >= 1 ||!sotware_mixer_enabled) {
			if(!isAirplay)
			{
				return packet.getAudio();
			}
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
		if (volume >= 1 ||!sotware_mixer_enabled) {
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
		if (volume >= 1 ||!sotware_mixer_enabled) {
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
		if (volume >= 1 ||!sotware_mixer_enabled) {
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
				mastervolume = ev.getVolume();
				volume = setVolume( calculateVolume());
			} catch (Exception ex) {
				log.error(ex);
			}
			break;
		case EVENTAIRPLAYVOLUMECHANGED:
			EventAirplayVolumeChanged eva = (EventAirplayVolumeChanged) e;
			try
			{
				airplayVolume = eva.getVolume();
				volume = setVolume( calculateVolume());
			}
			catch(Exception ex)
			{
				log.error(ex);
			}
			break;
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
	
	private long calculateVolume()
	{ 
		if(!isAirplay)
		{
			return mastervolume;
		}
		
		if(!sotware_mixer_enabled)
		{
			return airplayVolume;
		}
		
		long res = mastervolume - (100 - airplayVolume);
		if(res < 0 )
		{
			res = 0;
		}
		if(res > 100)
		{
			res = 100;
		}
		log.debug("masterVolume: " + mastervolume + " airplayVolume: " + airplayVolume +" volume: " + volume);
		return res;
	}
	
	
	private float setVolumeTAN(double v)
	{
		v = v -20;
		double res = Math.tan(v/100.0);
		if(res > 1)
		{
			res = 1;
		}
		if(res < 0)
		{
			res = 0;
		}
		log.debug("Volume: " + v +" Converted to : " + res);
		return (float)res;
	}
	
	/*
	 * Because the volume isn't linear, fudge the values a bit.
	 * How I wish I had listened in my maths class when they were talking about logarithms!!
	 */
	private float setVolume(double vv)
	{
		int v = (int)vv;
		float res = 100;
		switch(v)
		{
		case 100:
			res = 1f;
			break;
		case 99:
			res = 0.97f;
			break;
		case 98:
			res = 0.94f;
			break;
		case 97:
			res = 0.91f;
			break;
		case 96:
			res = 0.88f;
			break;
		case 95:
			res = 0.85f;
			break;
		case 94:
			res = 0.82f;
			break;
		case 93:
			res = 0.79f;
			break;
		case 92:
			res = 0.76f;
			break;
		case 91:
			res = 0.73f;
			break;
		case 90:
			res = 0.71f;
			break;

		case 89:
			res = 0.68f;
			break;
		case 88:
			res = 0.65f;
			break;
		case 87:
			res = 0.62f;
			break;
		case 86:
			res = 0.59f;
			break;
		case 85:
			res = 0.56f;
			break;
		case 84:
			res = 0.53f;
			break;
		case 83:
			res = 0.50f;
			break;
		case 82:
			res = 0.47f;
			break;
		case 81:
			res = 0.44f;
			break;
		case 80:
			res = 0.41f;
			break;
			
		case 79:
			res = 0.39f;
			break;
		case 78:
			res = 0.37f;
			break;
		case 77:
			res = 0.35f;
			break;
		case 76:
			res = 0.33f;
			break;
		case 75:
			res = 0.31f;
			break;
		case 74:
			res = 0.29f;
			break;
		case 73:
			res = 0.27f;
			break;
		case 72:
			res = 0.25f;
			break;
		case 71:
			res = 0.23f;
			break;
		case 70:
			res = 0.21f;
			break;
		
		case 69:
			res = 0.20f;
			break;
		case 68:
			res = 0.19f;
			break;
		case 67:
			res = 0.18f;
			break;
		case 66:
			res = 0.17f;
			break;
		case 65:
			res = 0.16f;
			break;
		case 64:
			res = 0.15f;
			break;
		case 63:
			res = 0.14f;
			break;
		case 62:
			res = 0.13f;
			break;
		case 61:
			res = 0.12f;
			break;
		case 60:
			res = 0.11f;
			break;
			
		case 59:
			res = 0.105f;
			break;
		case 58:
			res = 0.10f;
			break;
		case 57:
			res = 0.095f;
			break;
		case 56:
			res = 0.08f;
			break;
		case 55:
			res = 0.075f;
			break;
		case 54:
			res = 0.07f;
			break;
		case 53:
			res = 0.065f;
			break;
		case 52:
			res = 0.06f;
			break;
		case 51:
			res = 0.055f;
			break;
		case 50:
			res = 0.04f;
			break;
			
		case 49:
			res = 0.03f;
			break;
		case 48:
			res = 0.035f;
			break;
		case 47:
			res = 0.02f;
			break;
		case 46:
			res = 0.025f;
			break;
		case 45:
			res = 0.020f;
			break;
		case 44:
			res = 0.025f;
			break;
		case 43:
			res = 0.010f;
			break;
		case 42:
			res = 0.009f;
			break;
		case 41:
			res = 0.008f;
			break;
		case 40:
			res = 0.007f;
			break;
			
		case 39:
			res = 0.006f;
			break;
		case 38:
			res = 0.005f;
			break;
		case 37:
			res = 0.004f;
			break;
		case 36:
			res = 0.0039f;
			break;
		case 35:
			res = 0.0038f;
			break;
		case 34:
			res = 0.0037f;
			break;
		case 33:
			res = 0.0036f;
			break;
		case 32:
			res = 0.0035f;
			break;
		case 31:
			res = 0.0034f;
			break;
		case 30:
			res = 0.0033f;
			break;
			
		case 29:
			res = 0.0032f;
			break;
		case 28:
			res = 0.0031f;
			break;
		case 27:
			res = 0.0030f;
			break;
		case 26:
			res = 0.0029f;
			break;
		case 25:
			res = 0.0028f;
			break;
		case 24:
			res = 0.0027f;
			break;
		case 23:
			res = 0.0026f;
			break;
		case 22:
			res = 0.0025f;
			break;
		case 21:
			res = 0.0024f;
			break;
		case 20:
			res = 0.0023f;
			break;
			
		case 19:
			res = 0.0022f;
			break;
		case 18:
			res = 0.0021f;
			break;
		case 17:
			res = 0.0020f;
			break;
		case 16:
			res = 0.0019f;
			break;
		case 15:
			res = 0.0018f;
			break;
		case 14:
			res = 0.0018f;
			break;
		case 13:
			res = 0.0016f;
			break;
		case 12:
			res = 0.0015f;
			break;
		case 11:
			res = 0.0014f;
			break;
		case 10:
			res = 0.0013f;
			break;
			
		case 9:
			res = 0.0012f;
			break;
		case 8:
			res = 0.0011f;
			break;
		case 7:
			res = 0.0010f;
			break;
		case 6:
			res = 0.0009f;
			break;
		case 5:
			res = 0.0008f;
			break;
		case 4:
			res = 0.0007f;
			break;
		case 3:
			res = 0.0006f;
			break;
		case 2:
			res = 0.0005f;
			break;
		case 1:
			res = 0.0004f;
			break;
		case 0:
			res = 0.0f;
			break;		
		}
		return res;
	}

}