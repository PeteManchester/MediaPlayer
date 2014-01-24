package org.rpi.player.events;


public class EventVolumeChanged implements EventBase {

//	public EventVolumeChanged(Object source) {
//		super(source);
//	}
	
	public EnumPlayerEvents getType()
	{
		return EnumPlayerEvents.EVENTVOLUMECHANGED;
	}
	
	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	private long volume = 0;
	

}
