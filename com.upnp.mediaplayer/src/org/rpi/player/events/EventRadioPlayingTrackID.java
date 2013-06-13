package org.rpi.player.events;

public class EventRadioPlayingTrackID implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTRADIOPLAYINGTRACKID;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private int id = -99;
	

}
