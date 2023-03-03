package org.rpi.player.events;

public class EventPlayListUpdateRepeat implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTPLAYLISTUPDATEREPEAT;
	}
	
	public boolean isRepeat() {
		return repeat;
	}

	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}

	private boolean repeat = false;
	

}