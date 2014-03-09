package org.rpi.songcast.core;

import org.rpi.songcast.ohm.OHMEventAudio;

public interface ISongcastPlayer extends Runnable{
		
	public void play();
	
	public void stop();
	
	public void put(OHMEventAudio event);

}
