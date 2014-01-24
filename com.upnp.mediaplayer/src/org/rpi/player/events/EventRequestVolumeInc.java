package org.rpi.player.events;

/**
 * Used to signal when a request to increase the volume has been made
 * @author phoyle
 *
 */

public class EventRequestVolumeInc implements EventBase {
	
	public EnumPlayerEvents getType()
	{
		return EnumPlayerEvents.EVENTREQUESTVOLUMEINC;
	}

}
