package org.rpi.songcast.ohu.receiver.messages;

import io.netty.buffer.ByteBuf;

import org.apache.log4j.Logger;
import org.rpi.java.sound.AudioInformation;
import org.rpi.java.sound.IAudioPacket;
import org.rpi.songcast.common.SongcastMessage;

//Offset    Bytes                   Desc
//0         1                       Msg Header Bytes (without the codec name)
//1         1                       Flags (lsb first: halt flag, lossless flag, timestamped flag all other bits 0)
//2         2                       Samples in this msg
//4         4                       Frame
//8         4                       Network timestamp
//12        4                       Media Latency
//16        4                       Media Timestamp
//20        8                       Sample Start (first sample's offset from the beginning of this track)
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

public class OHUMessageAudio extends SongcastMessage implements IAudioPacket {
	
	private Logger log = Logger.getLogger(this.getClass());
	private AudioInformation ai = null;
	private byte[] audio;
	private int frameNumber = 0;
	private long time_to_play = 0;
	private int attempts = 0;
	private int length = 0;

	public OHUMessageAudio(ByteBuf buf,boolean hasSlaves) {
		super.setData(buf.retain());
		int length = buf.getShort(6);
		int headerLength = buf.getByte(8) & ~0x80;
		int flags = buf.getByte(9) & ~0x80;
		int sampleCount = buf.getShort(10);
		frameNumber = buf.getInt(12);
		int latency = buf.getInt(20);
		int timeStamp = buf.getInt(24);
		long StartSample = buf.getLong(28);
		long TotalSamples = buf.getLong(36);
		int iSampleRate = buf.getInt(44);
		int bitRate = buf.getInt(48);
		int iBitDepth = buf.getByte(54) & ~0x80;
		int channels = buf.getByte(55) & ~0x80;
		int codecNameLength = buf.getByte(57) & ~0x80;
		
		
		
		if (bitRate > 0) {
			bitRate = bitRate / 1000;
		}
		long time = System.currentTimeMillis();
		if (latency > 0) {
			try {
				if (iSampleRate == 0) {
					iSampleRate = 44100;
				}
				long res = latency / iSampleRate;
				res = res * 1000;
				res = res / 256;
				time += res;
			} catch (Exception e) {
			}
		}
		if(hasSlaves)
		{
			//Add some latency if we are forwarding to other songcast receivers.
			time += 150 ;
		}
		setTimeToPlay(time);

		int soundStart = 8 + headerLength + codecNameLength;
		int soundEnd = -99;
		soundEnd = soundStart + ((channels * iBitDepth * sampleCount) / 8);
		int soundLength = soundEnd - soundStart;
		byte[] codec = new byte[codecNameLength];
		buf.getBytes(58, codec, 0, codecNameLength);
		String sCodec = "";
		try {
			sCodec = new String(codec, "UTF-8");
		} catch (Exception e) {

		}
		audio = (new byte[soundLength]);
		length = soundLength;
		if (buf.readableBytes() >= soundStart + soundLength) {
			buf.getBytes(soundStart, audio, 0, soundLength);
			setAudioInformation(new AudioInformation(iSampleRate, bitRate, iBitDepth, channels, sCodec, soundLength, sampleCount));
		}
		else
		{
			System.out.println("Bufer was too small: " + (soundStart + soundLength + " BufferSize: " + buf.readableBytes()));
		}
	}

	/**
	 * @return the ai
	 */
	public AudioInformation getAudioInformation() {
		return ai;
	}

	/**
	 * @param ai
	 *            the ai to set
	 */
	private void setAudioInformation(AudioInformation ai) {
		this.ai = ai;
	}

	/**
	 * @return the audio
	 */
	public byte[] getAudio() {
		return audio;
	}

	@Override
	public String toString() {
		return "OHUMessageAudio";
	}

	/**
	 * @return the frameNumber
	 */
	public int getFrameNumber() {
		return frameNumber;
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
	 * Increase the Number of attempts.
	 */
	@Override
	public void incAttempts() {
		attempts++;
	}

	/**
	 * Has this Event expired
	 * 
	 * @return
	 */
	@Override
	public boolean expired() {
		if (attempts > 500) {
			return true;
		}
		return false;
	}

	@Override
	public int getLength() {
		return length;
	}
}
