package org.rpi.player.events;

public class EventMuteChanged implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTMUTECHANGED;
	}
	
	public boolean isMute() {
		return mute;
	}

	public void setMute(boolean mute) {
		this.mute = mute;
	}

	private boolean mute = false;
	
	

}
