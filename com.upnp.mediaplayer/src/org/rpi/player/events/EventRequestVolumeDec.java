package org.rpi.player.events;
/**
 * Used to signal when a request to decrease the volume has been made
 * @author phoyle
 *
 */

public class EventRequestVolumeDec implements EventBase {
	
	public EnumPlayerEvents getType()
	{
		return EnumPlayerEvents.EVENTREQUESTVOLUMEDEC;
	}

}
