package org.rpi.player.events;

public class EventRadioPlayPrevious implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTRADIOPLAYPREVIOUS;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String name = "";
	

}
