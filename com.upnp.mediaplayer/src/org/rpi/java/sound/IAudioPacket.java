package org.rpi.java.sound;

import org.scratchpad.songcast.core.AudioInformation;

public interface IAudioPacket {	

	public AudioInformation getAudioInformation() ;

	public byte[] getAudio(); 

	public int getFrameNumber() ;

	public long getTimeToPlay() ;

	public void incAttempts() ;
	
	public boolean expired() ;
	
	public int getLength();

}
