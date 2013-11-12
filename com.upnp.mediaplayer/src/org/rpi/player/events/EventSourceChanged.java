package org.rpi.player.events;
/**
 * The Source has been Changed
 * @author phoyle
 *
 */

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
