package org.rpi.player.events;


public class EventLoaded implements EventBase {
	
//	public EventLoaded(Object source) {
//		super(source);
//
//	}
	
	public EnumPlayerEvents getType()
	{
		return EnumPlayerEvents.EVENTLOADED;
	}

}
