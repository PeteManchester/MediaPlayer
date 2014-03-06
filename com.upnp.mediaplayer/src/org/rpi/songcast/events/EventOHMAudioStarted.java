package org.rpi.songcast.events;

public class EventOHMAudioStarted implements EventSongCastBase {

	@Override
	public EnumSongCastEvents getType() {
		return EnumSongCastEvents.EVENT_OHM_AUDIO_STARTED;
	}
	
}
