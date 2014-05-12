package org.rpi.java.sound;

import org.scratchpad.songcast.core.AudioInformation;

public interface IJavaSoundPlayer extends Runnable {
	
	public void createSoundLine(AudioInformation audioInf);
	
	public void put(AudioPacket event);
	
	public void clear();
	
	public void stop();
	

}
