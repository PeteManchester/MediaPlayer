package org.rpi.songcast.ohm;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import org.rpi.mplayer.TrackInfo;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventUpdateTrackInfo;
import org.rpi.songcast.core.SongcastMessage;
import org.rpi.songcast.core.SongcastPlayerJavaSound;

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

	/**
	 * Get the Audio Data
	 */
	public void checkMessageType() {
		int headerLength = new BigInteger(getBytes(8,8)).intValue();
		int sampleCount = new BigInteger(getBytes(10, 11)).intValue();
		int iBitDepth = new BigInteger(getBytes(54, 54)).intValue();
		int channels = new BigInteger(getBytes(55, 55)).intValue();
		int codecNameLength = new BigInteger(getBytes(57, 57)).intValue();
		int soundStart = 8 + headerLength + codecNameLength;
		int soundEnd = soundStart + ((channels * iBitDepth * (sampleCount) / 8));
		byte[] sound = getBytes(soundStart, soundEnd - 1);

		SongcastPlayerJavaSound.getInstance().addData(sound);
	}

	/**
	 * Used to set the Track Info
	 */
	public void getTrackInfo() {
		TrackInfo info = new TrackInfo();
		long iBitDepth = new BigInteger(getBytes(54, 54)).longValue();
		info.setBitDepth(iBitDepth);
		long iBitRate = new BigInteger(getBytes(48, 51)).longValue();
		try
		{
			iBitRate = iBitRate /1000;
		}
		catch(Exception e)
		{
			
		}
		info.setBitrate(iBitRate);
		int codecNameLength = new BigInteger(getBytes(57, 57)).intValue();

		byte[] codec = getBytes(58, (58 + codecNameLength) - 1);
		String sCodec;
		try {
			sCodec = new String(codec, "UTF-8");
			info.setCodec(sCodec);
		} catch (UnsupportedEncodingException e) {
		}
		long iSampleRate = new BigInteger(getBytes(44, 47)).longValue();
		info.setSampleRate(iSampleRate);
		info.setDuration(0);
		EventUpdateTrackInfo ev = new EventUpdateTrackInfo();
		ev.setTrackInfo(info);
		PlayManager.getInstance().updateTrackInfo(ev);

	}
}
