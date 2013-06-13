package org.rpi.player.events;

public class EventPlayListUpdateShuffle implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTPLAYLISTUPDATESHUFFLE;
	}
	
	public boolean isShuffle() {
		return shuffle;
	}

	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
	}

	private boolean shuffle = false;
	

}
