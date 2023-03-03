
package org.rpi.player.events;

public class EventTransportUpdateShuffle implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTTRANSPORTUPDATEDSHUFFLE;
	}
	
	public boolean isShuffle() {
		return shuffle;
	}

	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
	}

	private boolean shuffle = false;
	

}