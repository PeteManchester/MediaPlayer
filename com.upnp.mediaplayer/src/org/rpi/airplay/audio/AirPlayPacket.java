package org.rpi.airplay.audio;

import org.rpi.java.sound.AudioInformation;
import org.rpi.java.sound.IAudioPacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class AirPlayPacket implements IAudioPacket {

	private byte[] audio;
	private int attempts = 0;
	private int length = 0;
	private int time_to_play = 0;
	AudioInformation audioInf = new AudioInformation(44100, 48, 16, 2, "ALAC", 0, 0);
	private ByteBuf alac = null;
	private boolean isLatencyEnabled = false;


	public AirPlayPacket() {
		//time_to_play += 2000;
	}

	public AirPlayPacket(ByteBuf buffer) {
		alac = Unpooled.directBuffer(buffer.capacity());
		this.alac.writeBytes(buffer);
	}

	public AirPlayPacket(boolean isLatencyEnabled) {
		this.isLatencyEnabled = isLatencyEnabled;
		if(isLatencyEnabled) {
			//Set some latency
			time_to_play = ((int)System.currentTimeMillis()) + 1000;
		}
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
		return audio;
	}

	@Override
	public int getFrameNumber() {
		return 0;
	}

	@Override
	public int getTimeToPlay() {
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

	@Override
	public boolean isLatencyEnabled() {
		return isLatencyEnabled ;
	}

}
