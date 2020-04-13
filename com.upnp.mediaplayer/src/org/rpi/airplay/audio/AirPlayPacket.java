package org.rpi.airplay.audio;

import org.rpi.java.sound.AudioInformation;
import org.rpi.java.sound.IAudioPacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class AirPlayPacket implements IAudioPacket {

	private ByteBuf audio;
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

	public AirPlayPacket(int latency) {

		if(latency > 0) {
			//Set some latency
			time_to_play = ((int)System.currentTimeMillis()) + latency;
		}
	}

	@Override
	public AudioInformation getAudioInformation() {
		return audioInf;
	}

	public void setAudio(ByteBuf audio) {
		this.audio = audio;
		length = audio.readableBytes() / 2;
	}

	@Override
	public ByteBuf getAudio() {
		return audio;
	}
	
	@Override
	public byte[] getAudioBytes() {
		byte[] bytes = this.audio.array();
		audio.release();
		return bytes;
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
		if(alac !=null)
		{
			int refCnt = alac.refCnt();
			if (refCnt > 0) {
				alac.release(refCnt);
			}
		}		
		
		if(audio !=null) {
			int refCnt = audio.refCnt();
			if (refCnt > 0) {
				audio.release(refCnt);
			}
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
