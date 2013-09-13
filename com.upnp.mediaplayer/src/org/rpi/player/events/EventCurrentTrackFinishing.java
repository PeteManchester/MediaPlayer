package org.rpi.player.events;

public class EventCurrentTrackFinishing implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTCURRENTTRACKFINISHING;
	}

}
