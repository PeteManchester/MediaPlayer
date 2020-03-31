package org.rpi.airplay.audio;

import org.rpi.airplay.AudioSession;
import org.rpi.airplay.AudioSessionHolder;
import org.rpi.alacdecoder.AlacDecodeUtils;
import org.rpi.alacdecoder.AlacFile;
import org.rpi.java.sound.AudioInformation;
import org.rpi.java.sound.IAudioPacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class AirPlayPacket implements IAudioPacket {

	private byte[] audio;
	private int attempts = 0;
	private int length = 0;
	private long time_to_play = System.currentTimeMillis();
	AudioInformation audioInf = new AudioInformation(44100, 48, 16, 2, "ALAC", 0, 0);
	private ByteBuf alac = null;
	// private int frame_size = 0;
	// private AlacFile alacFile = null;

	public AirPlayPacket() {
		time_to_play += 2000;
	}

	public AirPlayPacket(ByteBuf buffer) {
		alac = Unpooled.directBuffer(buffer.capacity());
		this.alac.writeBytes(buffer);
	}

	@Override
	public AudioInformation getAudioInformation() {
		return audioInf;
	}

	public void setAudio(byte[] audio) {
		this.audio = audio;
		length = audio.length / 2;
	}

	@Override
	public byte[] getAudio() {
		/*
		 * AudioSession session = AudioSessionHolder.getInstance().getSession();
		 * AlacFile alacFile = session.getAlac(); int frame_size =
		 * session.getFrameSize();
		 * 
		 * int[] outbuffer = new int[4 * (frame_size + 3)]; final byte[]
		 * alacBytes = new byte[alac.capacity() + 3]; alac.getBytes(0,
		 * alacBytes, 0, alac.capacity()); // Decode ALAC to PCM int outputsize
		 * = 0; outputsize = AlacDecodeUtils.decode_frame(alacFile, alacBytes,
		 * outbuffer, outputsize); // Convert int array to byte array byte[]
		 * input = new byte[outputsize * 2]; int j = 0; for (int ic = 0; ic <
		 * outputsize; ic++) { input[j++] = (byte) (outbuffer[ic] >> 8);
		 * input[j++] = (byte) (outbuffer[ic]); }
		 */

		return audio;
	}

	@Override
	public int getFrameNumber() {
		return 0;
	}

	@Override
	public long getTimeToPlay() {
		return time_to_play;
	}

	@Override
	public void incAttempts() {
		attempts++;
	}

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

	@Override
	public void release() {
		if(alac ==null)
		{
			return;
		}
		int refCnt = alac.refCnt();
		if (refCnt > 0) {
			alac.release(refCnt);
		}
	}

	public void setALAC(ByteBuf buffer) {
		alac = buffer;
	}

}
