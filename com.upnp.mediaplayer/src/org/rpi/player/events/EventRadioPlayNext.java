package org.rpi.player.events;

public class EventRadioPlayNext implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTRADIOPLAYNEXT;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String name = "";
	

}
