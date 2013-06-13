package org.rpi.player.events;

public class EventPlayListPlayingTrackID implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTPLAYLISTPLAYINGTRACKID;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private int id = -99;

}
