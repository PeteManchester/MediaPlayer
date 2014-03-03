package org.rpi.songcast;

import java.math.BigInteger;

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

	//private Logger log = Logger.getLogger(this.getClass());

	public void checkMessageType() {

		//int length = new BigInteger(getBytes(7, 8)).intValue();
		// byte[] protocol = getBytes(0, 3);
		// String sProtocol = byteToString(protocol);
		// byte[] type = getBytes(5, 5);
		// StringBuilder sb = new StringBuilder();
		// for (byte b : type) {
		// sb.append(String.format("%02X ", b));
		// }
		// String s = sb.toString().trim();
		// if (s.equalsIgnoreCase("03")) {
		// if (sProtocol.trim().toUpperCase().startsWith("OHM")) {

		// StringBuilder sba = new StringBuilder();
		// for (byte b : data) {
		// sba.append(String.format("%02X ", b));
		// }
		// log.debug("Audio Stream: " + sba.toString());

		// byte[] length = getBytes(6, 7);
		// int iLength = byteArrayToInt(length, 2);

		//int sampleCount = byteArrayToInt(getBytes(10, 11), 2);
		
		int sampleCount = new BigInteger(getBytes(10,11)).intValue();

		// byte[] frameNumber = getBytes(12, 15);
		// String hex = byteToHexString(frameNumber);
		// log.debug("HEX: " + hex);
		// int iFrameNumber = byteArrayToInt(frameNumber);
		// log.debug("FrameNumber: " + iFrameNumber);

		// StringBuilder sbl = new StringBuilder();
		// for (byte b : data) {
		// sbl.append(String.format("%02X ", b));
		// }
		// log.debug("Audio Stream: " + sbl.toString());

		// byte[] bitRate = getBytes(48, 51);
		// int iBitRate = byteArrayToInt(bitRate);

		// byte[] sampleRate = getBytes(44, 47);
		// int iSampleRate = byteArrayToInt(sampleRate);

		 //byte[] bitDepth = getBytes(54, 54);
		 //int iBitDepth = byteArrayToInt(bitDepth, 1);
		int iBitDepth = new BigInteger(getBytes(54,54)).intValue();
		
		int channels = new BigInteger(getBytes(55,55)).intValue();

		 //int channels = byteArrayToInt(getBytes(55, 55), 1);

		//int codecNameLength = byteArrayToInt(getBytes(57, 57), 1);
		
		int codecNameLength = new BigInteger(getBytes(57,57)).intValue();

		// byte[] codec = getBytes(58, (58 + codecNameLength) - 1);

		int soundStart = 58 + codecNameLength;
		// int soundEnd = 16392;// soundStart + (channels * iBitDepth *
		// (sampleCount/8)) -1;
		 int soundEnd = soundStart + ((channels * iBitDepth * (sampleCount) / 8));
		// int myLength = data.length;
		// log.debug("DataLength: " + myLength);
		 byte[] sound = getBytes(soundStart, soundEnd - 1);
		//byte[] sound = getBytes(soundStart, length - 1);
		// if (iLength < 1828) {
		// log.debug("Codec :" + byteToString(codec) + " Bit Rate: " + iBitRate
		// + " SampleRate: " + iSampleRate + " BitDepth: " + iBitDepth +
		// " FrameNumber: " + iFrameNumber + " Length: " + iLength);
		// try {
		// log.debug(new String(data,"UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		SongcastPlayerJavaSound.getInstance().addData(sound);
	}
	// } else {
	// log.debug("MesageType = " + s);
	// }
}
