package org.rpi.songcast;

import org.apache.log4j.Logger;
import org.rpi.songcast.events.EventOHMAudio;

public class OHMResponseJoin extends SongcastMessage {

	private Logger log = Logger.getLogger(this.getClass());

	public void checkMessageType() {
		byte[] protocol = getBytes(0, 3);
		String sProtocol = byteToString(protocol);
		byte[] type = getBytes(5, 5);
		StringBuilder sb = new StringBuilder();
		for (byte b : type) {
			sb.append(String.format("%02X ", b));
		}
		String s = sb.toString().trim();
		if (s.equalsIgnoreCase("03")) {
			if (sProtocol.trim().toUpperCase().startsWith("OHM")) {

				// StringBuilder sba = new StringBuilder();
				// for (byte b : data) {
				// sba.append(String.format("%02X ", b));
				// }
				// log.debug("Audio Stream: " + sba.toString());

				//byte[] length = getBytes(6, 7);
				//int iLength = byteArrayToInt(length, 2);

				//int sampleCount = byteArrayToInt(getBytes(10, 11), 2);

				//byte[] frameNumber = getBytes(12, 15);
				//String hex = byteToHexString(frameNumber);
				// log.debug("HEX: " + hex);
				//int iFrameNumber = byteArrayToInt(frameNumber);
				//log.debug("FrameNumber: " + iFrameNumber);

				//StringBuilder sbl = new StringBuilder();
				//for (byte b : data) {
				//	sbl.append(String.format("%02X ", b));
				//}
				// log.debug("Audio Stream: " + sbl.toString());

				//byte[] bitRate = getBytes(48, 51);
				//int iBitRate = byteArrayToInt(bitRate);

				//byte[] sampleRate = getBytes(44, 47);
				//int iSampleRate = byteArrayToInt(sampleRate);

				//byte[] bitDepth = getBytes(54, 54);
				//int iBitDepth = byteArrayToInt(bitDepth, 1);

				//int channels = byteArrayToInt(getBytes(55, 55), 1);

				int codecNameLength = byteArrayToInt(getBytes(57, 57), 1);

				//byte[] codec = getBytes(58, (58 + codecNameLength) - 1);

				int soundStart = 58 + codecNameLength;
				// int soundEnd = 16392;// soundStart + (channels * iBitDepth *
				// (sampleCount/8)) -1;
				//int soundEnd = soundStart + ((channels * iBitDepth * (sampleCount) / 8));
				//int myLength = data.length;
				// log.debug("DataLength: " + myLength);
				//byte[] sound = getBytes(soundStart, soundEnd - 1);
				byte[] sound = getBytes(soundStart, 1828-1 );
				//if (iLength < 1828) {
					//log.debug("Codec :" + byteToString(codec) + " Bit Rate: " + iBitRate + " SampleRate: " + iSampleRate + " BitDepth: " + iBitDepth + " FrameNumber: " + iFrameNumber + " Length: " + iLength);
					// try {
					// log.debug(new String(data,"UTF-8"));
					// } catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					// }
				//}
				SongcastPlayerJavaSound.getInstance().addData(sound);
			}
		} else {
			log.debug("MesageType = " + s);
		}
	}

}
