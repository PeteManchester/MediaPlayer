package org.rpi.player.events;

public class EventStopSongcast implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTSTOPSONGCAST;
	}

}
