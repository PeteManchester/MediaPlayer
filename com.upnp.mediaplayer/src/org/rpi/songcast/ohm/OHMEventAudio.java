package org.rpi.songcast.ohm;

import java.math.BigInteger;
import org.apache.log4j.Logger;
import org.rpi.songcast.core.AudioInformation;
import org.rpi.songcast.core.SongcastMessage;

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

	private long iSampleRate = -99;
	private long iBitRate = -99;
	private int iBitDepth = -99;
	private int channels = -99;
	private String sCodec = "Not Defined";
	private int sampleCount = -99;
	private int codecNameLength = -99;

	// private AudioInformation audioInfo = null;

	/**
	 * Get the Audio Data
	 */
	public void checkMessageType() {

		try {
			int headerLength = -99;
			headerLength = new BigInteger(getBytes(8, 8)).intValue();
			sampleCount = new BigInteger(getBytes(10, 11)).intValue();

			// long network_timestamp = new BigInteger(getBytes(16,
			// 19)).longValue();
			// log.debug("TimeStamp " + bytesToHex(getBytes(16,19)));
			long latency = -99;
			latency = new BigInteger(getBytes(20, 23)).intValue();
			long time = System.currentTimeMillis();
			iSampleRate = new BigInteger(getBytes(44, 47)).longValue();

			if (latency > 0) {

				try {
					latency = latency / 10000;
					if (iSampleRate == 0)
						iSampleRate = 44100;
					time = (latency * iSampleRate * 256) / 1000;
				} catch (Exception e) {
					log.error("Error calculating latency", e);
				}

			}
			setTimeToPlay(time);
			// long media_timestamp = new BigInteger(getBytes(24,
			// 27)).longValue();

			iBitDepth = new BigInteger(getBytes(54, 54)).intValue();
			channels = new BigInteger(getBytes(55, 55)).intValue();
			codecNameLength = new BigInteger(getBytes(57, 57)).intValue();

			int soundStart = -99;
			soundStart = 8 + headerLength + codecNameLength;
			int soundEnd = -99;
			soundEnd = soundStart + ((channels * iBitDepth * sampleCount) / 8);
			setSound(getBytes(soundStart, soundEnd - 1));

		} catch (Exception e) {
			log.error("Error Parsing Audio Message", e);
		}

	}

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
		try {
			iBitRate = new BigInteger(getBytes(48, 51)).longValue();
			iBitRate = iBitRate / 1000;
			byte[] codec = getBytes(58, (58 + codecNameLength) - 1);
			sCodec = new String(codec, "UTF-8");
			AudioInformation audioInfo = new AudioInformation(iSampleRate, iBitRate, iBitDepth, channels, sCodec, sound.length, sampleCount);
			return audioInfo;
		} catch (Exception e) {

		}
		return null;
	}
}
