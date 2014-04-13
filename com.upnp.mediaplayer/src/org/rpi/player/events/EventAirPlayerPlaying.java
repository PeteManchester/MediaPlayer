package org.rpi.player.events;



public class EventAirPlayerPlaying implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTAIRPLAYERPLAYING;
	}

}
