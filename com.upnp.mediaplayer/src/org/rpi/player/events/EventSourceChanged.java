package org.rpi.player.events;

public class EventSourceChanged implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTSOURCECHANGED;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String name = "";
	

}
