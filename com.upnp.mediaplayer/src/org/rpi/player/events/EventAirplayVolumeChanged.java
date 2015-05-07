package org.rpi.player.events;

public class EventAirplayVolumeChanged implements EventBase {
	
	public EnumPlayerEvents getType()
	{
		return EnumPlayerEvents.EVENTAIRPLAYVOLUMECHANGED;
	}
	
	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	private long volume = 0;

}
