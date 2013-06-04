package org.rpi.player.events;

import java.util.EventObject;

public class EventFinishedCurrentTrack extends EventObject {

	public EventFinishedCurrentTrack(Object source) {
		super(source);
	}
	
	//Did we force the stop..
	boolean bQuit = false;

	public void setQuit(boolean b) {
		bQuit = b;
		
	}
	
	public boolean isQuit()
	{
		return bQuit;
	}

}
