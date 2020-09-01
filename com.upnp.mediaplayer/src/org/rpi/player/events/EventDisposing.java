package org.rpi.player.events;

public class EventDisposing implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTDISPOSING;
	}
}
