package org.rpi.java.sound;

import io.netty.buffer.ByteBuf;

public interface IAudioPacket {	

	public AudioInformation getAudioInformation() ;

	public ByteBuf getAudio(); 
	
	public byte[] getAudioBytes();

	public int getFrameNumber() ;

	public int getTimeToPlay() ;

	public void incAttempts() ;
	
	public boolean expired() ;
	
	public int getLength();
	
	public void release();
	
	public boolean isLatencyEnabled();

}
