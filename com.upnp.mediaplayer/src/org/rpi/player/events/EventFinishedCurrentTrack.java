package org.rpi.player.events;


public class EventFinishedCurrentTrack implements EventBase {

//	public EventFinishedCurrentTrack(Object source) {
//		super(source);
//	}
	
	public EnumPlayerEvents getType()
	{
		return EnumPlayerEvents.EVENTFINISHEDCURRENTTRACK;
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
