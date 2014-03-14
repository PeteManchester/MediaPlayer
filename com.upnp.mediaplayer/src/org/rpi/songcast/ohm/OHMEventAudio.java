package org.rpi.songcast.ohm;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import org.apache.log4j.Logger;
import org.rpi.mplayer.TrackInfo;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventUpdateTrackInfo;
import org.rpi.songcast.core.AudioInformation;
import org.rpi.songcast.core.SongcastMessage;
import org.rpi.songcast.core.SongcastPlayerJSLatency;

//Offset    Bytes                   Desc
//0         1                       Msg Header Bytes (without the codec name)
//1         1                       Flags (lsb first: halt flag, lossless flag, timestamped flag all other bits 0)
//2         2                       Samples in this msg
//4         4                       Frame
//8         4                       Network timestamp
//12        4                       Media Latency
//16        4                       Media Timestamp
//20        8                       Sample Start (first sample's offset from the beginiing of this track)
//28        8                       Samples Total (total samples for this track)
//36        4                       Sample Rate
//40        4                       Bit Rate
//44		2						Volume Offset
//46        1                       Bit depth of audio (16, 24)
//47        1                       Channels
//48        1                       Reserved (must be zero)
//49        1                       Codec Name Bytes
//50        n                       Codec Name
//50 + n    Msg Total Bytes - Msg Header Bytes - Code Name Bytes (Sample data in big endian, channels interleaved, packed)

public class OHMEventAudio extends SongcastMessage {
	private Logger log = Logger.getLogger(this.getClass());

	private long time_to_play = 0;

	private byte[] sound = null;

	private int attempts = 0;
	
	private AudioInformation audioInfo = null;

	/**
	 * Get the Audio Data
	 */
	public void checkMessageType() {
		int headerLength = new BigInteger(getBytes(8, 8)).intValue();
		int sampleCount = new BigInteger(getBytes(10, 11)).intValue();
		//if(sampleCount !=1)
		//{
		//	log.info("Sample Count: " + sampleCount);
		//}
		// long network_timestamp = new BigInteger(getBytes(16,
		// 19)).longValue();
		// log.debug("TimeStamp " + bytesToHex(getBytes(16,19)));
		long latency = new BigInteger(getBytes(20, 23)).intValue();
		// log.debug("Latency: " + latency);
		long time = System.currentTimeMillis();
		
		long iSampleRate = new BigInteger(getBytes(44, 47)).longValue();
		
		if (latency > 0) {
			
			latency = latency / 10000;
			if (iSampleRate == 0)
				iSampleRate = 44100;
			time = (latency * iSampleRate * 256) / 1000;
		}
		setTimeToPlay(time);
		// long media_timestamp = new BigInteger(getBytes(24, 27)).longValue();
		long iBitRate = new BigInteger(getBytes(48, 51)).longValue();
		try {
			iBitRate = iBitRate / 1000;
		} catch (Exception e) {

		}		
		int iBitDepth = new BigInteger(getBytes(54, 54)).intValue();
		int channels = new BigInteger(getBytes(55, 55)).intValue();
		int codecNameLength = new BigInteger(getBytes(57, 57)).intValue();
		int soundStart = 8 + headerLength + codecNameLength;
		
		int soundEnd = soundStart + ((channels * iBitDepth * sampleCount) / 8);

		byte[] codec = getBytes(58, (58 + codecNameLength) - 1);		String sCodec = "";
		try {
			sCodec = new String(codec, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		setSound(getBytes(soundStart, soundEnd - 1));
		audioInfo = new AudioInformation(iSampleRate, iBitRate, iBitDepth, channels, sCodec, sound.length,sampleCount );
	}

//	/**
//	 * Used to set the Track Info
//	 */
//	public AudioInformation getTrackInfo() {
//		TrackInfo info = new TrackInfo();
//		long iBitDepth = new BigInteger(getBytes(54, 54)).longValue();
//
//		info.setBitDepth(iBitDepth);
//		long iBitRate = new BigInteger(getBytes(48, 51)).longValue();
//		try {
//			iBitRate = iBitRate / 1000;
//		} catch (Exception e) {
//
//		}
//		info.setBitrate(iBitRate);
//
//		int codecNameLength = new BigInteger(getBytes(57, 57)).intValue();
//
//		byte[] codec = getBytes(58, (58 + codecNameLength) - 1);
//		String sCodec = "";
//		try {
//			sCodec = new String(codec, "UTF-8");
//			info.setCodec(sCodec);
//		} catch (UnsupportedEncodingException e) {
//		}
//
//		long iSampleRate = new BigInteger(getBytes(44, 47)).longValue();
//		info.setSampleRate(iSampleRate);
//		info.setDuration(0);
//		int channels = new BigInteger(getBytes(55, 55)).intValue();
//		log.info("Songcast Stream: Codec: " + sCodec + " SampleRate: " + iSampleRate + " BitRate: " + iBitRate + " BitDepth: " + iBitDepth);
//		AudioInformation audioInf = new AudioInformation(iSampleRate, iBitRate, iBitDepth, channels, sCodec);
//		EventUpdateTrackInfo ev = new EventUpdateTrackInfo();
//		ev.setTrackInfo(info);
//		PlayManager.getInstance().updateTrackInfo(ev);
//		// Added by Pete
//		int headerLength = new BigInteger(getBytes(8, 8)).intValue();
//		int sampleCount = new BigInteger(getBytes(10, 11)).intValue();
//		int soundStart = 8 + headerLength + codecNameLength;
//		int soundEnd = soundStart + ((channels * (int) iBitDepth * (sampleCount) / 8));
//		int length = soundEnd - soundStart;
//		log.debug("Audio Length: " + length);
//		return audioInf;
//	}

	/**
	 * @return the time_to_play
	 */
	public long getTimeToPlay() {
		return time_to_play;
	}

	/**
	 * @param time_to_play
	 *            the time_to_play to set
	 */
	private void setTimeToPlay(long time_to_play) {
		this.time_to_play = time_to_play;
	}

	/**
	 * @return the sound
	 */
	public byte[] getSound() {
		return sound;
	}

	/**
	 * @param sound
	 *            the sound to set
	 */
	public void setSound(byte[] sound) {
		this.sound = sound;
	}

	/**
	 * Increase the Number of attempts.
	 */
	public void incAttempts() {
		attempts++;
	}

	/**
	 * Has this Event expired
	 * 
	 * @return
	 */
	public boolean expired() {
		if (attempts > 500) {
			return true;
		}
		return false;
	}

	/**
	 * @return the audioInfo
	 */
	public AudioInformation getAudioInfo() {
		return audioInfo;
	}

	/**
	 * @param audioInfo the audioInfo to set
	 */
	public void setAudioInfo(AudioInformation audioInfo) {
		this.audioInfo = audioInfo;
	}
}
