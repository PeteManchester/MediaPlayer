package org.rpi.airplay;

import org.rpi.java.sound.AudioPacket;
import org.scratchpad.songcast.core.AudioInformation;

public class AirPlayPacket implements AudioPacket {
	
	private byte[] audio;
	private int attempts = 0;
	private int length = 0;
	private long time_to_play = System.currentTimeMillis();
	AudioInformation audioInf = new AudioInformation(44100, 48, 16, 2, "ALAC", 0, 0);	
	
	public AirPlayPacket()
	{
		time_to_play += 2000;
	}

	@Override
	public AudioInformation getAudioInformation() {
		return audioInf;
	}
	
	public void setAudio(byte[] audio)
	{
		this.audio = audio;
		length = audio.length/2;
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

}
